package io.github.kaltrinabajramii.urbantransitbackend.model.entity;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

// ========== Ticket Usage Log (Track which routes were used) ==========

@Entity
@Table(name = "ticket_usage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportType transportType;

    @Column(name = "boarding_stop", length = 100)
    private String boardingStop;

    @Column(name = "destination_stop", length = 100)
    private String destinationStop;

    @CreationTimestamp
    @Column(name = "used_at", updatable = false)
    private LocalDateTime usedAt;
}