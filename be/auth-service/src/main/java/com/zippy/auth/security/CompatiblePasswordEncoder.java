package com.zippy.auth.security;

import org.springframework.security.crypto.password.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** New hashes support long UTF-8 passwords; existing unprefixed BCrypt hashes still work. */
public final class CompatiblePasswordEncoder implements PasswordEncoder {
    private final BCryptPasswordEncoder legacy = new BCryptPasswordEncoder(12);
    private final DelegatingPasswordEncoder current = new DelegatingPasswordEncoder("pbkdf2",
        Map.of("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()));
    @Override public String encode(CharSequence raw) { return current.encode(raw); }
    @Override public boolean matches(CharSequence raw, String encoded) {
        if (raw == null || encoded == null || encoded.isBlank()) return false;
        if (encoded.startsWith("$2")) {
            if (raw.toString().getBytes(StandardCharsets.UTF_8).length > 72) return false;
            return legacy.matches(raw, encoded);
        }
        try { return current.matches(raw, encoded); }
        catch (IllegalArgumentException ex) { return false; }
    }
}
