package com.zippy.auth.repository;
import com.zippy.auth.domain.entity.User;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("select u.id from User u where u.email = :email")
    Optional<UUID> findIdByEmail(@org.springframework.data.repository.query.Param("email") String email);
    @Query("select u.password from User u where u.id = :id")
    Optional<String> findPasswordById(@org.springframework.data.repository.query.Param("id") UUID id);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findLockedById(@org.springframework.data.repository.query.Param("id") UUID id);
    boolean existsByEmail(String email);
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);
    @Override @EntityGraph(attributePaths = "roles")
    Optional<User> findById(UUID id);
}
