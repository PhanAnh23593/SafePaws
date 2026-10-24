package com.zippy.auth;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.*;
import com.zippy.auth.config.*;
import org.junit.jupiter.api.*;
import org.springframework.security.oauth2.jwt.*;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class GoogleTokenVerificationTest {
    private static RSAKey key;
    private NimbusJwtDecoder decoder;
    private final Instant now = Instant.parse("2026-09-24T00:00:00Z");
    @BeforeAll static void keys() throws Exception { key = new RSAKeyGenerator(2048).generate(); }
    @BeforeEach void setup() throws Exception {
        decoder = NimbusJwtDecoder.withPublicKey(key.toRSAPublicKey()).build();
        decoder.setJwtValidator(GoogleConfig.validator(new GoogleProperties(List.of("client")), Clock.fixed(now, ZoneOffset.UTC)));
    }
    private String signed(RSAKey signingKey, String issuer, String audience, Instant expiry, String subject) throws Exception {
        var claims = new JWTClaimsSet.Builder().issuer(issuer).audience(audience).subject(subject)
            .issueTime(Date.from(now.minusSeconds(10)));
        if (expiry != null) claims.expirationTime(Date.from(expiry));
        var token = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims.build());
        token.sign(new RSASSASigner(signingKey));
        return token.serialize();
    }
    @Test void validGoogleIssuers() throws Exception {
        for (String issuer : List.of("accounts.google.com", "https://accounts.google.com"))
            assertThat(decoder.decode(signed(key, issuer, "client", now.plusSeconds(300), "subject")).getSubject()).isEqualTo("subject");
    }
    @Test void rejectsSignatureIssuerAudienceExpiryAndMissingClaims() throws Exception {
        List<String> invalid = List.of(
            signed(new RSAKeyGenerator(2048).generate(), "accounts.google.com", "client", now.plusSeconds(300), "subject"),
            signed(key, "https://attacker.example", "client", now.plusSeconds(300), "subject"),
            signed(key, "accounts.google.com", "other-client", now.plusSeconds(300), "subject"),
            signed(key, "accounts.google.com", "client", now.minusSeconds(120), "subject"),
            signed(key, "accounts.google.com", "client", null, "subject"),
            signed(key, "accounts.google.com", "client", now.plusSeconds(300), null));
        for (String token : invalid) assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }
}
