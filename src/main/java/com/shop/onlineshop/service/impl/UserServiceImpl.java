package com.shop.onlineshop.service.impl;

import com.shop.onlineshop.dataservices.RoleDataService;
import com.shop.onlineshop.dataservices.UserEntityDataService;
import com.shop.onlineshop.exceptions.UserAlreadyExistsException;
import com.shop.onlineshop.models.dto.UserDTO;
import com.shop.onlineshop.models.entity.Role;
import com.shop.onlineshop.models.entity.UserEntity;
import com.shop.onlineshop.models.request.ChangePasswordRequest;
import com.shop.onlineshop.models.request.LoginRequest;
import com.shop.onlineshop.models.request.OtpVerifyRequest;
import com.shop.onlineshop.models.response.JWTResponse;
import com.shop.onlineshop.models.response.OtpSentResponse;
import com.shop.onlineshop.security.service.JWTService;
import com.shop.onlineshop.service.OtpService;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserEntityDataService userData;
    private final RoleDataService roleData;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final OtpService otpService;

    @Value("${onlineshop.app.isproduction}")
    private boolean isProduction;

    public UserServiceImpl(
            UserEntityDataService userData,
            RoleDataService roleData,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authManager,
            JWTService jwtService,
            OtpService otpService
    ) {
        this.userData = userData;
        this.roleData = roleData;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    @Override
    public UserDTO register(UserDTO dto) {

        if (userData.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userData.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }
        if (dto.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        Role role = roleData.findByName("ROLE_USER");
        user.setRoles(List.of(role));

        userData.saveUserEntity(user);

        dto.setPassword(null);
        return dto;
    }


    @Override
    public OtpSentResponse login(LoginRequest request, HttpServletResponse response) {

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        UserEntity user = userData.getUserEntityByUsernameOrThrow(request.username());
        otpService.generateAndAssign(user);
        userData.updateUserEntity(user);

        return new OtpSentResponse("OTP sent", 300);
    }

    @Override
    public JWTResponse verifyOtp(OtpVerifyRequest request, HttpServletResponse response) {

        UserEntity user = userData.getUserEntityByUsernameOrThrow(request.username());

        if (!otpService.verify(user, request.otp())) {
            userData.updateUserEntity(user);

            if (user.getOtpAttempts() >= 5) {
                throw new BadCredentialsException("Too many OTP attempts. Request a new OTP.");
            }

            throw new BadCredentialsException("Invalid or expired OTP");
        }

        otpService.clear(user);
        userData.updateUserEntity(user);

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        user.getRoles().stream()
                                .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r.getName()))
                                .toList()
                );

        return issueTokens(response, auth);
    }

    @Override
    public void changePassword(ChangePasswordRequest req) {

        UserEntity user = getCurrentUser();

        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid old password");
        }
        if (req.newPassword().length() < 8) {
            throw new IllegalArgumentException("Password too short");
        }

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userData.updateUserEntity(user);
    }

    @Override
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(isProduction);
        cookie.setPath("/api/v1");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @Override
    public JWTResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {

        String refresh = extractRefreshToken(request);
        if (refresh == null || jwtService.isTokenExpired(refresh)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String username = jwtService.extractUserName(refresh);
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, null);

        return issueTokens(response, auth);
    }

    @Override
    public UserEntity getCurrentUser() {
        String username =
                SecurityContextHolder.getContext().getAuthentication().getName();
        return userData.getUserEntityByUsernameOrThrow(username);
    }

    private JWTResponse issueTokens(HttpServletResponse response, Authentication auth) {

        String access = jwtService.generateToken(auth);
        String refresh = jwtService.generateRefreshToken(auth);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", refresh)
                .httpOnly(true)
                .secure(isProduction)
                .path("/api/v1")
                .sameSite(isProduction ? "Strict" : "Lax")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return new JWTResponse("Bearer", access);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if ("refresh_token".equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
