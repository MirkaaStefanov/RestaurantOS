package com.example.RestaurantOS.services.impl.security;

import com.example.RestaurantOS.config.rateLimiting.RateLimiterConfigProperties;
import com.example.RestaurantOS.enums.TokenType;
import com.example.RestaurantOS.exceptions.email.EmailNotVerified;
import com.example.RestaurantOS.exceptions.token.ExpiredTokenException;
import com.example.RestaurantOS.exceptions.token.InvalidTokenException;
import com.example.RestaurantOS.exceptions.user.UserLoginException;
import com.example.RestaurantOS.exceptions.user.UserNotFoundException;
import com.example.RestaurantOS.models.dto.auth.AuthenticationRequest;
import com.example.RestaurantOS.models.dto.auth.AuthenticationResponse;
import com.example.RestaurantOS.models.dto.auth.PublicUserDTO;
import com.example.RestaurantOS.models.dto.auth.RegisterRequest;
import com.example.RestaurantOS.models.entity.Token;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.models.entity.VerificationToken;
import com.example.RestaurantOS.repositories.UserRepository;
import com.example.RestaurantOS.repositories.VerificationTokenRepository;
import com.example.RestaurantOS.services.AuthenticationService;
import com.example.RestaurantOS.services.JwtService;
import com.example.RestaurantOS.services.TokenService;
import com.example.RestaurantOS.services.UserService;
import com.example.RestaurantOS.utils.PasswordEncryptionUtils;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserService userService;
    private final TokenService tokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiterConfigProperties rateLimiterConfigProperties;

    /**
     * Registers a new user based on the provided registration request.
     */
    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        User user = userService.createUser(request);

        userRepository.save(user);

        return tokenService.generateAuthResponse(user);
    }

    // Login with correct email and password
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user;

        try {
            user = userService.findByEmail(request.getEmail());
        } catch (UserNotFoundException userNotFoundException) {
            throw new UserLoginException();
        }

        boolean passedFirstCheck = PasswordEncryptionUtils.validatePassword(request.getPassword(), user.getPassword());

        if (passedFirstCheck) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);

            if (!user.isEnabled()) {
                throw new EmailNotVerified();
            }
        } else {
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );
            } catch (DisabledException exception) {
                throw new EmailNotVerified();
            } catch (AuthenticationException exception) {
                throw new UserLoginException();
            }
        }

        tokenService.revokeAllUserTokens(user);
        return tokenService.generateAuthResponse(user);
    }

    /**
     * Generates a new access token and updates the refresh token based on the provided refresh token.
     * If the refresh token is missing or invalid, it throws an InvalidTokenException.
     * If the refresh token is valid, it generates a new access token, revokes all existing user tokens,
     * and updates the refresh token to the provided one.
     */
    @Override
    public AuthenticationResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new InvalidTokenException();
        }

        String userEmail;

        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (JwtException exception) {
            throw new InvalidTokenException();
        }

        if (userEmail == null) {
            throw new InvalidTokenException();
        }

        // Make sure token is a refresh token not access token
        Token token = tokenService.findByToken(refreshToken);
        if (token != null && token.tokenType != TokenType.REFRESH) {
            throw new InvalidTokenException();
        }

        User user = userService.findByEmail(userEmail);

        if (!jwtService.isTokenValid(refreshToken, user)) {
            tokenService.revokeToken(token);
            throw new InvalidTokenException();
        }

        String accessToken = jwtService.generateToken(user);

        tokenService.revokeAllUserTokens(user);
        tokenService.saveToken(user, accessToken, TokenType.ACCESS);
        tokenService.saveToken(user, refreshToken, TokenType.REFRESH);

        return AuthenticationResponse
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Retrieves user information based on the provided JWT token.
     * If the token is invalid or missing, it throws an InvalidTokenException.
     * If the token is valid, it retrieves the user's access and refresh tokens, updates the refresh token if necessary,
     * and returns an authentication response containing the user's information and tokens.
     */
    @Override
    public PublicUserDTO me(String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new InvalidTokenException();
        }
        String bearerToken = jwtToken;

        String realToken = "";
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            realToken = bearerToken.substring(7); // "Bearer ".length() is 7
        }


        Token accessToken = tokenService.findByToken(realToken);

        if (accessToken == null) {
            throw new InvalidTokenException();
        }

        User user = accessToken.getUser();

        boolean isTokenValid;

        try {
            isTokenValid = jwtService.isTokenValid(accessToken.getToken(), user);
        } catch (JwtException jwtException) {
            isTokenValid = false;
        }

        if (!isTokenValid) {
            tokenService.revokeAllUserTokens(user);
            throw new InvalidTokenException();
        }

        List<Token> tokens = tokenService.findByUser(user);
        List<Token> refreshTokens = tokens.stream().filter(x -> x.getTokenType() == TokenType.REFRESH).toList();

        if (refreshTokens.isEmpty()) {
            throw new InvalidTokenException();
        }

        Token refreshToken = refreshTokens.get(0);

        if (refreshToken == null) {
            throw new InvalidTokenException();
        }

        String refreshTokenString;

        if (!jwtService.isTokenValid(refreshToken.getToken(), user)) {
            refreshTokenString = jwtService.generateRefreshToken(user);
            tokenService.saveToken(user, refreshTokenString, TokenType.REFRESH);
        } else {
            refreshTokenString = refreshToken.getToken();
        }

        return modelMapper.map(accessToken.getUser(), PublicUserDTO.class);

    }

    /**
     * Resets the password for a user based on the provided token and new password.
     */
    public void resetPassword(String token, String newPassword) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            throw new InvalidTokenException();
        }


        User user = verificationToken.getUser();
        if (user == null) {
            throw new InvalidTokenException();
        }

        verificationToken.setCreatedAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    public void confirmRegistration(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            throw new ExpiredTokenException();
        }

        verificationToken.setCreatedAt(LocalDateTime.now());

        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            throw new ExpiredTokenException();
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);

        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    public User forgotPassword(String email) {
        User user = userService.findByEmail(email);

        if (!user.isEnabled()) {
            throw new EmailNotVerified();
        }

        return user;
    }
}
