package com.example.RestaurantOS.services;


import com.example.RestaurantOS.models.dto.auth.AuthenticationRequest;
import com.example.RestaurantOS.models.dto.auth.AuthenticationResponse;
import com.example.RestaurantOS.models.dto.auth.PublicUserDTO;
import com.example.RestaurantOS.models.dto.auth.RegisterRequest;
import com.example.RestaurantOS.models.entity.User;

import java.io.IOException;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse refreshToken(String refreshToken) throws IOException;

    PublicUserDTO me(
            String jwtToken
    );

    void resetPassword(String token, String newPassword);

    void confirmRegistration(String verificationToken);

    User forgotPassword(String email);
}
