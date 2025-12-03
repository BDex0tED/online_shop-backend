package com.shop.onlineshop.controller;

import com.shop.onlineshop.models.dto.UserDTO;
import com.shop.onlineshop.models.entity.UserEntity;
import com.shop.onlineshop.models.request.ChangePasswordRequest;
import com.shop.onlineshop.models.request.LoginRequest;
import com.shop.onlineshop.models.response.JWTResponse;
import com.shop.onlineshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserEntityController {

  private final UserService userService;

  public UserEntityController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/register")
  public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDTO) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(userDTO));
  }

  @PostMapping("/login")
  public ResponseEntity<JWTResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
    return ResponseEntity.ok(userService.login(loginRequest, response));
  }

  @PostMapping("/change-password")
  public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
    userService.changePassword(request);
    return ResponseEntity.ok("Password changed successfully");
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpServletResponse response) {
    userService.logout(response);
    return ResponseEntity.ok("Logout successful");
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<JWTResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
    return ResponseEntity.ok(userService.refreshToken(request, response));
  }
}
