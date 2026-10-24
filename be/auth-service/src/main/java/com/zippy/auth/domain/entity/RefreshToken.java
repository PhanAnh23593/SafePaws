package com.zippy.auth.domain.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name = "refresh_tokens", indexes = {
    @Index(name = "ix_refresh_tokens_user", columnList = "user_id"),
    @Index(name = "ix_refresh_tokens_session", columnList = "user_id,session_id"),
    @Index(name = "ix_refresh_tokens_expiry", columnList = "expires_at")
})
@EntityListeners(AuditingEntityListener.class) @Getter @Setter
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(nullable = false) private UUID sessionId;
    @Column(nullable = false) private Instant expiresAt;
    @Column(nullable = false) private boolean revoked;
    @CreatedDate @Column(nullable = false, updatable = false) private Instant createdAt;
}
