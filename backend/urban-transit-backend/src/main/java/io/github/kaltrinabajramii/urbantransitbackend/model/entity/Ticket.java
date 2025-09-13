package io.github.kaltrinabajramii.urbantransitbackend.model.entity;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketStatus;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// ========== Ticket Entity ==========

@Entity
@Table(name = "ticket")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 50)
    private String ticketNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType ticketType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.ACTIVE;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "used_date")
    private LocalDateTime usedDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to check if ticket is currently valid
    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        return status == TicketStatus.ACTIVE
                && now.isAfter(validFrom)
                && now.isBefore(validUntil)
                && (ticketType != TicketType.RIDE || usedDate == null);
    }

    // Helper method to check if ticket can be used for any transit
    public boolean canBeUsedForTransit() {
        if (ticketType == TicketType.RIDE) {
            // Single ride ticket - can only be used once
            return isCurrentlyValid() && usedDate == null;
        } else {
            // Monthly/Yearly tickets - unlimited rides within validity period
            return isCurrentlyValid();
        }
    }

    // Helper method to use the ticket (for single rides)
    public void useTicket() {
        if (ticketType == TicketType.RIDE && canBeUsedForTransit()) {
            this.usedDate = LocalDateTime.now();
            this.status = TicketStatus.USED;
        }
        // Monthly/Yearly tickets don't change status when used
    }
}