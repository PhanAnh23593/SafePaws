package com.zippy.auth.domain.entity;
import com.zippy.auth.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
@Entity @Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
@Getter @Setter
public class User extends BaseEntity {
    @Column(nullable = false, length = 254)
    private String email;
    @Column(nullable = false, length = 10)
    private String phone;
    @Column(nullable = false, length = 255)
    private String password;
    @Column(nullable = false, columnDefinition = "text")
    private String fullName;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}
