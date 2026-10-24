package com.zippy.auth.service;

/** Server-managed flow credentials travel in X-Auth-Flow, never in screen DTOs. */
public interface OtpService {
    enum Purpose { REGISTER, RESET_PASSWORD }
    record Registration(String email, String fullName, String phone, String encodedPassword) {
        @Override public String toString() { return "Registration[redacted]"; }
    }
    record Claim(String flowToken, String previous, String reserved, Registration registration) {
        @Override public String toString() { return "Claim[redacted]"; }
    }
    String start(Purpose purpose, Registration registration);
    void resend(String flowToken, Purpose purpose);
    Claim claimRegistration(String flowToken, String otp);
    void verifyReset(String flowToken, String otp);
    Claim claimReset(String flowToken);
    void finish(Claim claim, boolean committed);
}
