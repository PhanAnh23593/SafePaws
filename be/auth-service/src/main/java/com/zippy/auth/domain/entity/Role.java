package com.zippy.auth.domain.entity;
import com.zippy.auth.domain.enums.RoleName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
@Entity @Table(name = "roles") @Getter @Setter
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, unique = true, length = 32) private RoleName name;
}
