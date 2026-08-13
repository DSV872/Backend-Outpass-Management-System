package com.outpass.outpass_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "outpass_parent_approval")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutpassParentApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "outpass_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_parent_approval_outpass")
    )
    private Outpass outpass;

    @Column(name = "parent_email", nullable = false, length = 100)
    private String parentEmail;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approval_token_hash", length = 255)
    private String approvalTokenHash;

    @Column(name = "approval_token_expires_at")
    private LocalDateTime approvalTokenExpiresAt;
}