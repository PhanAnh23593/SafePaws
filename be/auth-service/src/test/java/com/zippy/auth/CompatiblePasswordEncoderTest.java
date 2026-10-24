package com.zippy.auth;

import com.zippy.auth.security.CompatiblePasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.assertj.core.api.Assertions.*;

class CompatiblePasswordEncoderTest {
    private final CompatiblePasswordEncoder encoder = new CompatiblePasswordEncoder();
    @Test void longUnicodePasswordRoundTripsWithoutTruncation() {
        String password = "Đ".repeat(80) + "!";
        String encoded = encoder.encode(password);
        assertThat(encoded).startsWith("{pbkdf2}").hasSizeLessThanOrEqualTo(255);
        assertThat(encoder.matches(password, encoded)).isTrue();
        assertThat(encoder.matches(password + "x", encoded)).isFalse();
    }
    @Test void existingBcryptAccountsStillWorkAndDoNotTruncate() {
        String password = "A!" + "x".repeat(70);
        String encoded = new BCryptPasswordEncoder(4).encode(password);
        assertThat(encoder.matches(password, encoded)).isTrue();
        assertThat(encoder.matches(password + "x", encoded)).isFalse();
        assertThat(encoder.matches("invalid", encoded)).isFalse();
    }
}
