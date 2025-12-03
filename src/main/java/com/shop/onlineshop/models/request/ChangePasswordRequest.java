package com.shop.onlineshop.models.request;

public record ChangePasswordRequest(String oldPassword,
                                    String newPassword) {}
