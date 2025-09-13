package io.github.kaltrinabajramii.urbantransitbackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    // Read secret from application.yml
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // Token validity: 24 hours (in milliseconds)
    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Creates a cryptographic key from our secret string
     * This key is used to sign and verify JWT tokens
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * CREATES a JWT token for a user
     * Input: user's email
     * Output: JWT token string (like "eyJ0eXAiOiJKV1Q...")
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        log.debug("Generating JWT token for user: {}", email);

        return Jwts.builder()
                .subject (email)              // Who the token is for
                .issuedAt (now)               // When it was created
                .expiration (expiryDate)      // When it expires
                .signWith (getSigningKey())  // Sign it
                .compact();                     // Convert to string
    }

    /**
     * EXTRACTS email from a JWT token
     * Input: JWT token string
     * Output: user's email
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith (getSigningKey())     // Use same key to verify
                    .build()
                    .parseSignedClaims (token)              // Parse and verify
                    .getPayload ();                         // Get the payload

            return claims.getSubject();                 // Extract email
        } catch (Exception e) {
            log.error("Error extracting email from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * VALIDATES a JWT token
     * Input: JWT token string
     * Output: true if valid, false if invalid/expired/tampered
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser ()
                    .verifyWith (getSigningKey())         // Use same key
                    .build()
                    .parseSignedClaims (token);                 // This throws exception if invalid

            log.debug("JWT token is valid");
            return true;

        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token format: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
