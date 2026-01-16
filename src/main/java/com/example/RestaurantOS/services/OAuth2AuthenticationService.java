package com.example.RestaurantOS.services;


import com.example.RestaurantOS.models.dto.auth.AuthenticationResponse;

public interface OAuth2AuthenticationService {

    String getOAuthGoogleLoginUrl();

    AuthenticationResponse processOAuthGoogleLogin(String code);

    // НОВ МЕТОД: За Mobile & Modern Web
    AuthenticationResponse processGoogleIdToken(String idTokenString);
}
