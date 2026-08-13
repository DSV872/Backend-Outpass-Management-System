package com.outpass.outpass_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "OUTPASS_EMAIL_RESENDS",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UK_OUTPASS_RECIPIENT",
            columnNames = {"OUTPASS_ID", "RECIPIENT"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class OutpassEmailResend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "OUTPASS_ID",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_EMAIL_RESEND_OUTPASS")
    )
    private Outpass outpass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailRecipient recipient;

    @Column(nullable = false)
    private Integer resendCount = 0;
    
}
