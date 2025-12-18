package com.shop.onlineshop.models.response;

public record LoginResponse(
        String message,
        Integer expiresInSeconds,
        String accessToken,
        String refreshToken
) {
}
