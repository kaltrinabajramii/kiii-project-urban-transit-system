package io.github.kaltrinabajramii.urbantransitbackend.service.impl;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.LoginRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.RegisterRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.AuthResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.UserResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.User;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.UserRole;
import io.github.kaltrinabajramii.urbantransitbackend.repository.UserRepository;
import io.github.kaltrinabajramii.urbantransitbackend.security.JwtUtils;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of AuthService for handling user authentication operations
 * Manages user registration, login, and JWT token operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Register a new user account
     */
    @Override
    public ResponseEntity<AuthResponse> register(RegisterRequest registerRequest) {
        log.info("Attempting to register new user with email: {}", registerRequest.getEmail());

        try {
            // Check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                log.warn("Registration failed: Email already exists - {}", registerRequest.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new AuthResponse(null, "Email address is already registered", null));
            }

            // Create new user entity
            User newUser = new User();
            newUser.setEmail(registerRequest.getEmail().toLowerCase().trim());
            newUser.setFullName(registerRequest.getFullName().trim());
            newUser.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
            newUser.setRole(UserRole.USER); // Default role
            newUser.setActive(true);

            // Save user to database
            User savedUser = userRepository.save(newUser);
            log.info("User registered successfully: {}", savedUser.getEmail());

            // Generate JWT token for immediate login
            String jwtToken = jwtUtils.generateToken(savedUser.getEmail());

            // Create user response DTO
            UserResponse userResponse = mapToUserResponse(savedUser);

            // Create auth response
            AuthResponse authResponse = new AuthResponse(jwtToken, userResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);

        } catch (Exception e) {
            log.error("Error during user registration for email: {}", registerRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Registration failed due to server error", null));
        }
    }

    /**
     * Authenticate user and generate JWT token
     */
    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getEmail());

        try {
            // Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail().toLowerCase().trim(),
                            loginRequest.getPassword()
                    )
            );

            // Get user details from database
            User user = userRepository.findByEmail(loginRequest.getEmail().toLowerCase().trim())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            // Check if user account is active
            if (!user.getActive()) {
                log.warn("Login failed: User account is deactivated - {}", loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AuthResponse(null, "Account has been deactivated", null));
            }

            // Generate JWT token
            String jwtToken = jwtUtils.generateToken(user.getEmail());

            // Create user response DTO
            UserResponse userResponse = mapToUserResponse(user);

            // Create auth response
            AuthResponse authResponse = new AuthResponse(jwtToken, userResponse);

            log.info("User logged in successfully: {}", user.getEmail());
            return ResponseEntity.ok(authResponse);

        } catch (BadCredentialsException e) {
            log.warn("Login failed: Invalid credentials for user - {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Invalid email or password", null));
        } catch (DisabledException e) {
            log.warn("Login failed: Account disabled for user - {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse(null, "Account is disabled", null));
        } catch (AuthenticationException e) {
            log.error("Authentication error for user: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Authentication failed", null));
        } catch (Exception e) {
            log.error("Error during login for user: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Login failed due to server error", null));
        }
    }

    /**
     * Validate JWT token and check if it's still valid
     */
    @Override
    public boolean validateToken(String token) {
        try {
            boolean isValid = jwtUtils.validateToken(token);
            log.debug("Token validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.error("Error validating token", e);
            return false;
        }
    }

    /**
     * Extract user email from JWT token
     */
    @Override
    public String getEmailFromToken(String token) {
        try {
            String email = jwtUtils.getEmailFromToken(token);
            log.debug("Email extracted from token: {}", email);
            return email;
        } catch (Exception e) {
            log.error("Error extracting email from token", e);
            throw new RuntimeException("Invalid token");
        }
    }

    /**
     * Refresh JWT token (generate new token for existing user)
     */
    @Override
    public ResponseEntity<AuthResponse> refreshToken(String token) {
        log.info("Attempting to refresh token");

        try {
            // Validate current token
            if (!jwtUtils.validateToken(token)) {
                log.warn("Token refresh failed: Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, "Invalid or expired token", null));
            }

            // Extract email from current token
            String email = jwtUtils.getEmailFromToken(token);

            // Get user from database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user is still active
            if (!user.getActive()) {
                log.warn("Token refresh failed: User account is deactivated - {}", email);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AuthResponse(null, "Account has been deactivated", null));
            }

            // Generate new token
            String newToken = jwtUtils.generateToken(user.getEmail());

            // Create user response DTO
            UserResponse userResponse = mapToUserResponse(user);

            // Create auth response
            AuthResponse authResponse = new AuthResponse(newToken, userResponse);

            log.info("Token refreshed successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            log.error("Error during token refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Token refresh failed", null));
        }
    }

    /**
     * Check if email is already registered
     */
    @Override
    public boolean isEmailRegistered(String email) {
        try {
            boolean exists = userRepository.existsByEmail(email.toLowerCase().trim());
            log.debug("Email registration check for {}: {}", email, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error checking email registration for: {}", email, e);
            return false;
        }
    }

    /**
     * Logout user (currently just returns success message)
     * In future, could implement token blacklisting
     */
    @Override
    public ResponseEntity<String> logout(String token) {
        log.info("User logout requested");

        try {
            // TODO: Implement token blacklisting if needed
            // For now, just log the logout attempt
            if (jwtUtils.validateToken(token)) {
                String email = jwtUtils.getEmailFromToken(token);
                log.info("User logged out: {}", email);
                return ResponseEntity.ok("Logged out successfully");
            } else {
                return ResponseEntity.badRequest().body("Invalid token");
            }
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Logout failed");
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFullName(user.getFullName());
        userResponse.setRole(user.getRole());
        userResponse.setActive(user.getActive());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;
    }

    /**
     * Validate registration request data
     */
    private void validateRegistrationRequest(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
    }

    /**
     * Validate login request data
     */
    private void validateLoginRequest(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}