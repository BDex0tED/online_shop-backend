package com.shop.onlineshop.models.response;

public record OtpSentResponse(
        String message,
        int expiresInSeconds
) {}
