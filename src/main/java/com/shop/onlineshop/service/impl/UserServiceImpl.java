package com.shop.onlineshop.service.impl;

import com.shop.onlineshop.dataservices.RoleDataService;
import com.shop.onlineshop.dataservices.UserEntityDataService;
import com.shop.onlineshop.exceptions.UserAlreadyExistsException;
import com.shop.onlineshop.models.entity.Role;
import com.shop.onlineshop.models.entity.UserEntity;
import com.shop.onlineshop.models.request.ChangePasswordRequest;
import com.shop.onlineshop.models.dto.UserDTO;
import com.shop.onlineshop.models.request.LoginRequest;
import com.shop.onlineshop.models.response.JWTResponse;
import com.shop.onlineshop.security.service.JWTService;
import com.shop.onlineshop.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
  private final UserEntityDataService userEntityDataService;
  private final RoleDataService roleDataService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authManager;
  private final JWTService jwtService;


  @Value( "${onlineshop.app.isproduction}")
  private boolean isProduction;

  public UserServiceImpl(UserEntityDataService userEntityDataService,
                         PasswordEncoder passwordEncoder,
                         RoleDataService roleDataService,
                         AuthenticationManager authManager,
                         JWTService jwtService) {
    this.userEntityDataService = userEntityDataService;
    this.passwordEncoder = passwordEncoder;
    this.roleDataService = roleDataService;
    this.authManager = authManager;
    this.jwtService = jwtService;
  }

  @Override
  public UserDTO register(UserDTO userDTO) {
    if(userEntityDataService.existsByUsername(userDTO.getUsername())){
      System.out.println("Username already exists");
      throw new UserAlreadyExistsException("Username already exists");
    }
    if(userEntityDataService.existsByEmail(userDTO.getEmail())){
      System.out.println("Email already was registered");
      throw new UserAlreadyExistsException("Email already was registered");
    }
    if(userDTO.getUsername() == null || userDTO.getPassword() == null || userDTO.getEmail() == null){
      throw new IllegalArgumentException("Invalid username/email/password");
    }
    if(userDTO.getPassword().length()<8){
      throw new IllegalArgumentException("Password must be at least 8 characters long");
    }
    UserEntity userEntity = new UserEntity();
    userEntity.setUsername(userDTO.getUsername());
    userEntity.setEmail(userDTO.getEmail());
    userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));

    List<Role> userRoles = new ArrayList<>();
    Role userRole = roleDataService.findByName("ROLE_USER");
    userRoles.add(userRole);
    userEntity.setRoles(userRoles);

    userEntityDataService.saveUserEntity(userEntity);

    userDTO.setPassword(null);
    return userDTO;

  }

  @Override
  public JWTResponse login(LoginRequest loginRequest, HttpServletResponse response) {
    try{
      Authentication authentication = authManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
      );
      return getJwtResponse(response, authentication);
    } catch (AuthenticationException e){
      throw new BadCredentialsException("Invalid username or password");
    }
  }

  @Override
  public void changePassword(ChangePasswordRequest changePasswordRequest){
    if(changePasswordRequest.oldPassword() == null || changePasswordRequest.newPassword() == null){
      throw new IllegalArgumentException("Invalid old password or new password");
    }if(changePasswordRequest.oldPassword().equals(changePasswordRequest.newPassword())){
      throw new IllegalArgumentException("Old password and new password are the same");
    }
    if(changePasswordRequest.newPassword().length() < 8){
      throw new IllegalArgumentException("Password must be at least 8 characters long");
    }
    UserEntity userEntity = getCurrentUser();
    if(!passwordEncoder.matches(changePasswordRequest.oldPassword(), userEntity.getPassword())){
      throw new BadCredentialsException("Invalid old password");
    }
    if(passwordEncoder.matches(changePasswordRequest.newPassword(), userEntity.getPassword())){
      throw new IllegalArgumentException("New password must be different from old password");
    }
    userEntity.setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
    userEntityDataService.updateUserEntity(userEntity);
  }

  @Override
  public void logout(HttpServletResponse response){
    Cookie cookie = new Cookie("refresh_token", null);
    cookie.setHttpOnly(true);
    cookie.setSecure(isProduction);
    cookie.setPath("/api/v1");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  @Override
  public JWTResponse refreshToken(HttpServletRequest request,
                                  HttpServletResponse response){
    try{
      String refreshToken = getRefreshTokenFromCookie(request);
      if(refreshToken == null){
        log.warn("Refresh token is null");
        throw new BadCredentialsException("Refresh token is missing");
      }
      String username = jwtService.extractUserName(refreshToken);
      if(username == null || jwtService.isTokenExpired(refreshToken)){
        log.warn("Refresh attempt failed for user={} from IP={}", username, request.getRemoteAddr());
        throw new BadCredentialsException("Invalid refresh token");
      }
      Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, null);

      return getJwtResponse(response, authentication);

    } catch(Exception e){
      throw new BadCredentialsException("Invalid refresh token");
    }

  }

  @Override
  public UserEntity getCurrentUser(){
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return userEntityDataService.getUserEntityByUsernameOrThrow(username);
  }

  private JWTResponse getJwtResponse(HttpServletResponse response, Authentication authentication) {
    String newAccessToken = jwtService.generateToken(authentication);
    String newRefreshToken = jwtService.generateRefreshToken(authentication);
    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", newRefreshToken)
      .httpOnly(true)
      .secure(isProduction)
      .path("/api/v1")
      .sameSite(isProduction ? "Strict" : "Lax")
      .maxAge(Duration.ofDays(7))
      .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    return new JWTResponse("Bearer ", newAccessToken);
  }


  private String getRefreshTokenFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("refresh_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

}
