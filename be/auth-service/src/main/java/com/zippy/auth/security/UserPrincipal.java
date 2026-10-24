package com.zippy.auth.security;
import com.zippy.auth.domain.entity.User;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;
public record UserPrincipal(UUID id, String email, String password,
                            List<GrantedAuthority> authorities) implements UserDetails {
    public static UserPrincipal from(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
            .<GrantedAuthority>map(r -> new SimpleGrantedAuthority(r.getName().name())).toList();
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), authorities);
    }
    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String toString() { return "UserPrincipal[redacted]"; }
}
