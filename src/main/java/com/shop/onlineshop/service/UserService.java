package com.shop.onlineshop.service;

import com.shop.onlineshop.models.entity.UserEntity;
import com.shop.onlineshop.models.request.ChangePasswordRequest;
import com.shop.onlineshop.models.dto.UserDTO;
import com.shop.onlineshop.models.request.LoginRequest;
import com.shop.onlineshop.models.request.OtpVerifyRequest;
import com.shop.onlineshop.models.response.JWTResponse;
import com.shop.onlineshop.models.response.OtpSentResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    UserDTO register(UserDTO userDTO);
    OtpSentResponse login(LoginRequest loginRequest, HttpServletResponse response);
    JWTResponse verifyOtp(OtpVerifyRequest request, HttpServletResponse response);
    void changePassword(ChangePasswordRequest changePasswordRequest);
    void logout(HttpServletResponse response);
    JWTResponse refreshToken(HttpServletRequest request, HttpServletResponse response);
    UserEntity getCurrentUser();
}
