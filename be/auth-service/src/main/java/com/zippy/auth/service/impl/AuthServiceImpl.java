package com.zippy.auth.service.impl;

import com.zippy.auth.config.AuthProperties;
import com.zippy.auth.domain.dto.request.*;
import com.zippy.auth.domain.dto.response.*;
import com.zippy.auth.domain.entity.*;
import com.zippy.auth.domain.enums.RoleName;
import com.zippy.auth.exception.*;
import com.zippy.auth.repository.*;
import com.zippy.auth.security.*;
import com.zippy.auth.service.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.*;
import java.util.*;

@Service @Transactional
public class AuthServiceImpl implements AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwords;
    private final OtpService otp;
    private final TokenService tokens;
    private final JwtService jwt;
    private final TokenBlacklist blacklist;
    private final AuthProperties properties;
    private final GoogleIdentityVerifier google;
    private final AuthRateLimiter rateLimiter;
    private final String dummyHash;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(UserRepository users, RoleRepository roles, PasswordEncoder passwords,
            TokenService tokens, JwtService jwt, AuthProperties properties,
            GoogleIdentityVerifier google, OtpService otp, TokenBlacklist blacklist, AuthRateLimiter rateLimiter) {
        this.users = users; this.roles = roles; this.passwords = passwords; this.tokens = tokens;
        this.jwt = jwt; this.properties = properties; this.google = google;
        this.otp = otp; this.blacklist = blacklist;
        this.rateLimiter = rateLimiter;
        this.dummyHash = passwords.encode(UUID.randomUUID().toString());
    }

    @Override @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public RegistrationStarted register(RegisterRequest request) {
        String email = normalize(request.email());
        if (users.existsByEmail(email)) throw new AppException(ErrorCode.AUTH_DUPLICATE_EMAIL);
        String flow = otp.start(OtpService.Purpose.REGISTER,
            new OtpService.Registration(email, request.fullName(), request.phone(), passwords.encode(request.password())));
        return new RegistrationStarted(new UserResponse(email, request.fullName()), flow);
    }

    @Override public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());
        rateLimiter.check("login", email, 10, java.time.Duration.ofMinutes(15));
        var user = users.findByEmail(email);
        boolean matches = passwords.matches(request.password(), user.map(User::getPassword).orElse(dummyHash));
        if (!matches || user.isEmpty()) throw new BadCredentialsException("Invalid credentials");
        return response(tokens.issue(user.get()));
    }

    @Override public void verifyRegistration(String flow, OtpRequest request) {
        OtpService.Claim claim = otp.claimRegistration(flow, request.otp());
        finishAfterTransaction(claim);
        var pending = claim.registration();
        if (users.existsByEmail(pending.email())) throw new AppException(ErrorCode.AUTH_DUPLICATE_EMAIL);
        Role role = roles.findByName(RoleName.ROLE_USER)
            .orElseThrow(() -> new IllegalStateException("Default ROLE_USER is missing; apply schema migrations"));
        User user = new User();
        user.setEmail(pending.email()); user.setFullName(pending.fullName()); user.setPhone(pending.phone());
        user.setPassword(pending.encodedPassword()); user.setRoles(Set.of(role));
        users.saveAndFlush(user);
    }

    @Override @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void resendRegistrationOtp(String flow) { otp.resend(flow, OtpService.Purpose.REGISTER); }

    @Override @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String forgotPassword(ForgotPasswordRequest request) {
        String email = normalize(request.email());
        rateLimiter.check("recovery", email, 5, java.time.Duration.ofMinutes(15));
        var user = users.findByEmail(email);
        // Unknown accounts receive an indistinguishable flow, but never receive mail.
        return otp.start(OtpService.Purpose.RESET_PASSWORD,
            new OtpService.Registration(email, null, null, user.map(User::getPassword).orElse(null)));
    }

    @Override @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void verifyForgotPassword(String flow, OtpRequest request) { otp.verifyReset(flow, request.otp()); }

    @Override @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void resendForgotPasswordOtp(String flow) { otp.resend(flow, OtpService.Purpose.RESET_PASSWORD); }

    @Override public void resetPassword(String flow, ResetPasswordRequest request) {
        OtpService.Claim claim = otp.claimReset(flow);
        finishAfterTransaction(claim);
        UUID userId = users.findIdByEmail(claim.registration().email())
            .orElseThrow(() -> new AppException(ErrorCode.AUTH_EMAIL_NOT_FOUND));
        User user = users.findLockedById(userId).orElseThrow(() -> new AppException(ErrorCode.AUTH_EMAIL_NOT_FOUND));
        // A proof issued before a previous password change is no longer valid.
        if (!Objects.equals(user.getPassword(), claim.registration().encodedPassword()))
            throw new AppException(ErrorCode.AUTH_RESET_SESSION_EXPIRED);
        user.setPassword(passwords.encode(request.newPassword()));
        users.saveAndFlush(user);
        tokens.revokeAll(user.getId());
    }

    private void finishAfterTransaction(OtpService.Claim claim) {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
            throw new IllegalStateException("OTP completion requires a database transaction");
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                try { otp.finish(claim, status == STATUS_COMMITTED); }
                catch (RuntimeException ex) {
                    // Never restore a consumed proof after commit. PROCESSING remains unusable until expiry.
                    log.error("Auth flow finalization failed: {}", ex.getClass().getSimpleName());
                }
            }
        });
    }

    @Override public AuthResponse google(String credential) {
        String email = google.verify(credential);
        User user = users.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.AUTH_GOOGLE_ACCOUNT_UNAVAILABLE));
        return response(tokens.issue(user));
    }
    @Override public AuthResponse refresh(String rawToken) { return response(tokens.rotate(rawToken)); }

    @Override public void logout(String accessToken) {
        if (accessToken == null) throw new AppException(ErrorCode.AUTH_UNAUTHORIZED);
        var decoded = jwt.decode(accessToken);
        UUID userId = UUID.fromString(decoded.getSubject());
        tokens.revokeSession(userId, jwt.sessionId(decoded));
        blacklist.revoke(accessToken, decoded.getExpiresAt());
    }

    @Override public UserResponse me(UUID id) {
        return users.findById(id).map(UserResponse::from).orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));
    }
    private AuthResponse response(TokenService.IssuedRefreshToken issued) {
        return new AuthResponse(jwt.create(issued.user(), issued.sessionId()), issued.rawToken(), "Bearer",
            properties.accessExpirationSeconds(), UserResponse.from(issued.user()));
    }
    private String normalize(String email) { return email.strip().toLowerCase(Locale.ROOT); }
}
