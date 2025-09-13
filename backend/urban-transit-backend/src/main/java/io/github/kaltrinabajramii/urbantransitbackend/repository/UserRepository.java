package io.github.kaltrinabajramii.urbantransitbackend.repository;

import io.github.kaltrinabajramii.urbantransitbackend.model.entity.User;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository< User,Long> {
    // ===== AUTHENTICATION (Required for JWT login) =====

    /**
     * Find user by email - used for login authentication
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email exists - used for registration validation
     */
    Boolean existsByEmail(String email);

    // ===== USER MANAGEMENT (Required for profile & admin) =====

    /**
     * Find users by role - used for admin user management
     */
    List <User> findByRole(UserRole role);

    /**
     * Find active users with pagination - used for admin user list
     */
    Page<User> findByActiveTrue(Pageable pageable);

    /**
     * Search users by name or email - used for admin user search
     */
    @Query("SELECT u FROM User u WHERE u.active = true AND " +
            "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY u.createdAt DESC")
    Page <User> searchActiveUsers(@Param ("searchTerm") String searchTerm, Pageable pageable);

    // ===== ANALYTICS (Required for dashboard) =====

    /**
     * Find users without tickets - used for marketing analytics
     */
    @Query("SELECT u FROM User u WHERE u.active = true AND " +
            "NOT EXISTS (SELECT t FROM Ticket t WHERE t.user = u)")
    Page<User> findUsersWithoutTickets(Pageable pageable);
}
