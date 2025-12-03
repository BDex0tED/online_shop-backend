package com.shop.onlineshop.service;

import com.shop.onlineshop.models.entity.UserEntity;
import com.shop.onlineshop.models.request.ChangePasswordRequest;
import com.shop.onlineshop.models.dto.UserDTO;
import com.shop.onlineshop.models.request.LoginRequest;
import com.shop.onlineshop.models.response.JWTResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface UserService {
  UserDTO register(UserDTO userDTO);
  JWTResponse login(LoginRequest loginRequest, HttpServletResponse response);
  void changePassword(ChangePasswordRequest changePasswordRequest);
  void logout(HttpServletResponse response);
  JWTResponse refreshToken(HttpServletRequest request, HttpServletResponse response);
  UserEntity getCurrentUser();

}
