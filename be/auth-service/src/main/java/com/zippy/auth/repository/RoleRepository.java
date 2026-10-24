package com.zippy.auth.repository;
import com.zippy.auth.domain.entity.Role;
import com.zippy.auth.domain.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleName name);
}
