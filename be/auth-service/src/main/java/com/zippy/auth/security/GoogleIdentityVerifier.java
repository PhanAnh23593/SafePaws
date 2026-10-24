package com.zippy.auth.security;

import com.zippy.auth.config.GoogleProperties;
import com.zippy.auth.exception.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class GoogleIdentityVerifier {
    private final JwtDecoder decoder;
    private final GoogleProperties properties;
    public GoogleIdentityVerifier(@Qualifier("googleJwtDecoder") JwtDecoder decoder, GoogleProperties properties) {
        this.decoder = decoder; this.properties = properties;
    }
    public String verify(String credential) {
        if (properties.clientIds() == null || properties.clientIds().stream().allMatch(String::isBlank))
            throw new AppException(ErrorCode.AUTH_GOOGLE_UNAVAILABLE);
        try {
            Jwt token = decoder.decode(credential);
            String email = token.getClaimAsString("email");
            String hostedDomain = token.getClaimAsString("hd");
            String authorizedParty = token.getClaimAsString("azp");
            if (authorizedParty != null && !properties.clientIds().contains(authorizedParty)) throw invalid();
            if (token.getAudience().size() > 1 && authorizedParty == null) throw invalid();
            if (!Boolean.TRUE.equals(token.getClaimAsBoolean("email_verified")) || email == null
                    || !(email.endsWith("@gmail.com") || (hostedDomain != null && !hostedDomain.isBlank())))
                throw invalid();
            return email.toLowerCase(Locale.ROOT);
        } catch (JwtException | IllegalArgumentException ex) { throw invalid(); }
    }
    private AppException invalid() { return new AppException(ErrorCode.AUTH_INVALID_GOOGLE_CREDENTIAL); }
}
