package com.zippy.auth.service;

public interface MailService {
    void sendOtp(String email, String otp, OtpService.Purpose purpose);
}
