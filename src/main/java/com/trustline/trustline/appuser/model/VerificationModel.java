package com.trustline.trustline.appuser.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "verifications")
public class VerificationModel extends AuditModel {
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String messageId;
    private UUID userId;
    private String pin;
    @Enumerated(EnumType.STRING)
    private OtpModeEnum mode;

}
