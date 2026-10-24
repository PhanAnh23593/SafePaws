package com.zippy.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.zippy.auth.config.AuthProperties;
import com.zippy.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    private final byte[] key = new byte[32];
    private final Instant now = Instant.parse("2026-10-02T00:00:00Z");
    private final UUID userId = UUID.randomUUID();
    private final JwtService jwt = new JwtService(new AuthProperties(
        Base64.getEncoder().encodeToString(key), "zippy-auth", 900, 3600, List.of()),
        Clock.fixed(now, ZoneOffset.UTC));

    private String signed(byte[] secret, String issuer, String audience, String subject, Instant expiry) throws Exception {
        var claims = new JWTClaimsSet.Builder().issuer(issuer).audience(audience).subject(subject)
            .issueTime(Date.from(now.minusSeconds(1)));
        if (expiry != null) claims.expirationTime(Date.from(expiry));
        var token = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        token.sign(new MACSigner(secret));
        return token.serialize();
    }

    @Test void acceptsValidInternalToken() throws Exception {
        assertThat(jwt.subject(signed(key, "zippy-auth", "zippy-api", userId.toString(), now.plusSeconds(60))))
            .isEqualTo(userId);
    }

    @Test void rejectsWrongSignatureIssuerAudienceExpiredOrMissingClaims() throws Exception {
        byte[] otherKey = new byte[32]; otherKey[0] = 1;
        for (String token : List.of(
                signed(otherKey, "zippy-auth", "zippy-api", userId.toString(), now.plusSeconds(60)),
                signed(key, "other-issuer", "zippy-api", userId.toString(), now.plusSeconds(60)),
                signed(key, "zippy-auth", "other-api", userId.toString(), now.plusSeconds(60)),
                signed(key, "zippy-auth", "zippy-api", userId.toString(), now.minusSeconds(1)),
                signed(key, "zippy-auth", "zippy-api", userId.toString(), null),
                signed(key, "zippy-auth", "zippy-api", null, now.plusSeconds(60)))) {
            assertThatThrownBy(() -> jwt.subject(token)).isInstanceOf(JwtException.class);
        }
    }

    @Test void rejectsNonUuidSubject() throws Exception {
        String token = signed(key, "zippy-auth", "zippy-api", "not-a-user-id", now.plusSeconds(60));
        assertThatThrownBy(() -> jwt.subject(token)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void invalidOrShortDeploymentSecretFailsAtStartup() {
        for (String secret : List.of("not-base64!", Base64.getEncoder().encodeToString(new byte[31])))
            assertThatThrownBy(() -> new JwtService(new AuthProperties(secret, "issuer", 900, 3600, List.of()),
                Clock.systemUTC())).isInstanceOf(IllegalStateException.class).hasMessageNotContaining(secret);
    }
}
