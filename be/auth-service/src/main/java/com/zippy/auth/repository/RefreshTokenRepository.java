package com.zippy.auth.repository;
import com.zippy.auth.domain.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    List<RefreshToken> findByUserIdAndSessionId(UUID userId, UUID sessionId);
    @Query("select t.user.id from RefreshToken t where t.tokenHash = :hash")
    Optional<UUID> findUserIdByHash(@Param("hash") String hash);
    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.user.id = :userId")
    void revokeAllForUser(@Param("userId") UUID userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshToken t where t.tokenHash = :hash")
    Optional<RefreshToken> findLockedByHash(@Param("hash") String hash);
}
