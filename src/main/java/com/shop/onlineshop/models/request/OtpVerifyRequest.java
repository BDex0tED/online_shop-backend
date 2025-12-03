package com.shop.onlineshop.models.request;

public record OtpVerifyRequest(
        String username,
        String otp
) {}
