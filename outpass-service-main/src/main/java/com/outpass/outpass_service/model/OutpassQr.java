package com.outpass.outpass_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "outpass_qr",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_outpass_qr_token",
            columnNames = "qr_token"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutpassQr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "outpass_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_qr_outpass")
    )
    private Outpass outpass;

    @Column(
        name = "qr_token",
        nullable = false,
        unique = true,
        length = 255
    )
    private String qrToken;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}