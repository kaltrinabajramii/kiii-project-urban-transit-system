package io.github.kaltrinabajramii.urbantransitbackend.service.interfaces;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.LoginRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.RegisterRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.AuthResponse;
import org.springframework.http.ResponseEntity;

/**
 * Service interface for user authentication operations
 * Handles user registration, login, and token management
 */
public interface AuthService {

    /**
     * Register a new user account
     *
     * @param registerRequest User registration details
     * @return ResponseEntity containing authentication response with JWT token
     * @throws RuntimeException if email already exists or validation fails
     */
    ResponseEntity<AuthResponse> register(RegisterRequest registerRequest);

    /**
     * Authenticate user and generate JWT token
     *
     * @param loginRequest User login credentials
     * @return ResponseEntity containing authentication response with JWT token
     * @throws RuntimeException if credentials are invalid or user is inactive
     */
    ResponseEntity<AuthResponse> login(LoginRequest loginRequest);

    /**
     * Validate JWT token and check if it's still valid
     *
     * @param token JWT token to validate
     * @return true if token is valid and not expired, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Extract user email from JWT token
     *
     * @param token JWT token
     * @return email address from token claims
     * @throws RuntimeException if token is invalid
     */
    String getEmailFromToken(String token);

    /**
     * Refresh JWT token (if implementing token refresh functionality)
     *
     * @param token Current JWT token
     * @return ResponseEntity containing new authentication response with refreshed token
     */
    ResponseEntity<AuthResponse> refreshToken(String token);

    /**
     * Check if email is already registered
     *
     * @param email Email address to check
     * @return true if email exists, false otherwise
     */
    boolean isEmailRegistered(String email);

    /**
     * Logout user (invalidate token if using token blacklist)
     *
     * @param token JWT token to invalidate
     * @return ResponseEntity with logout confirmation message
     */
    ResponseEntity<String> logout(String token);
}