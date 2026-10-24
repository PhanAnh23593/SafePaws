package com.zippy.auth.security;
import com.zippy.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.UUID;
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    @Override public UserPrincipal loadUserByUsername(String identifier) {
        String normalized = identifier.strip().toLowerCase(Locale.ROOT);
        return users.findByEmail(normalized).map(UserPrincipal::from)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }
    public UserPrincipal loadById(UUID id) {
        return users.findById(id).map(UserPrincipal::from)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }
}
