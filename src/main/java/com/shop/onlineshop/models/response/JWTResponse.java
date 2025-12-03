package com.shop.onlineshop.models.response;

public record JWTResponse(String tokenType,
                          String accessToken) {}
