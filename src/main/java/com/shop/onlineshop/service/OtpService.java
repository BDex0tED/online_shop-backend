package com.shop.onlineshop.service;

import com.shop.onlineshop.models.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class OtpService {

    private final PasswordEncoder passwordEncoder;

    public OtpService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void generateAndAssign(UserEntity user) {
        String otp = String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 999999)
        );

        user.setOtpCode(passwordEncoder.encode(otp));
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(5));

        log.warn("OTP for {} = {}", user.getUsername(), otp);
    }

    public boolean verify(UserEntity user, String rawOtp) {
        if (user.getOtpCode() == null || user.getOtpExpiresAt() == null) {
            return false;
        }
        if (user.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        return passwordEncoder.matches(rawOtp, user.getOtpCode());
    }

    public void clear(UserEntity user) {
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
    }
}
