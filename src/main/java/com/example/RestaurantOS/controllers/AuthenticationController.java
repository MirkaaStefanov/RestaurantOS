package com.example.RestaurantOS.controllers;

import com.example.RestaurantOS.config.FrontendConfig;
import com.example.RestaurantOS.filters.JwtAuthenticationFilter;
import com.example.RestaurantOS.models.dto.auth.AuthenticationRequest;
import com.example.RestaurantOS.models.dto.auth.AuthenticationResponse;
import com.example.RestaurantOS.models.dto.auth.RegisterRequest;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.services.AuthenticationService;
import com.example.RestaurantOS.services.impl.security.events.OnPasswordResetRequestEvent;
import com.example.RestaurantOS.services.impl.security.events.OnRegistrationCompleteEvent;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Controller class for handling authentication-related operations.
 * JWT (access and refresh token);
 * OAuth2;
 * Email confirmation;
 * Forgotten password.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final ApplicationEventPublisher eventPublisher;
    private final FrontendConfig frontendConfig;
    private final ModelMapper modelMapper;

    @Value("${server.backend.baseUrl}")
    private String appBaseUrl;

    @PostMapping("/register")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        AuthenticationResponse authenticationResponse = authenticationService.register(request);

        // Email verification eventually
         User user = modelMapper.map(authenticationResponse.getUser(), User.class);
         eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, appBaseUrl));

        return ResponseEntity.ok(authenticationResponse);
    }

    //Endpoint for email confirmation during registration
    @GetMapping("/registrationConfirm")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<String> confirmRegistration(@RequestParam("token") String token, HttpServletResponse httpServletResponse) throws IOException {
        authenticationService.confirmRegistration(token);
        httpServletResponse.sendRedirect(frontendConfig.getLoginUrl());
        return ResponseEntity.ok("User registration confirmed successfully!");
    }

    @PostMapping("/authenticate")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request, HttpServletResponse servletResponse) {
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request);
        System.out.println("--- BACKEND: Entering loginUser method ---");
        System.out.println("Login attempt for email: " + request.getEmail()+request.getPassword());
        return ResponseEntity.ok(authenticationResponse);
    }

    @GetMapping("/refresh-token/{refreshToken}")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<AuthenticationResponse> refreshToken(@PathVariable String refreshToken) throws IOException {
        AuthenticationResponse authenticationResponse = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(authenticationResponse);
    }

    @GetMapping("/me") // Retrieves current user information.
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<AuthenticationResponse> getMe(@RequestHeader("Authorization") String auth, HttpServletRequest request) {
        String jwtToken = (String) request.getAttribute(JwtAuthenticationFilter.JWT_KEY);
        AuthenticationResponse authenticationResponse = authenticationService.me(auth);

        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping("/forgot-password") // Sends link to email so the user can change their password
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
        User user = authenticationService.forgotPassword(email);
        eventPublisher.publishEvent(new OnPasswordResetRequestEvent(user, appBaseUrl));
        return ResponseEntity.ok("Password reset link sent to your email!");
    }

    @PostMapping("/password-reset")
    @RateLimiter(name = "sensitive_operations_rate_limiter")
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
        authenticationService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password reset successfully");
    }
}
