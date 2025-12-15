package com.shop.onlineshop.service;

import com.shop.onlineshop.models.entity.UserEntity;
import com.shop.onlineshop.models.request.ChangePasswordRequest;
import com.shop.onlineshop.models.dto.UserDTO;
import com.shop.onlineshop.models.request.LoginRequest;
import com.shop.onlineshop.models.request.OtpVerifyRequest;
import com.shop.onlineshop.models.response.JWTResponse;
import com.shop.onlineshop.models.response.RegisterResponse;
import com.shop.onlineshop.models.response.OtpSentResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    RegisterResponse register(UserDTO userDTO);
    OtpSentResponse login(LoginRequest loginRequest, HttpServletResponse response);
    //might be changed
    JWTResponse verifyOtp(OtpVerifyRequest request);
    void changePassword(ChangePasswordRequest changePasswordRequest);
    void logout(HttpServletResponse response);
    UserEntity getCurrentUser();
}
