package com.example.RestaurantOS.services;


import com.example.RestaurantOS.models.dto.auth.AuthenticationResponse;

public interface OAuth2AuthenticationService {

    String getOAuthGoogleLoginUrl();

    AuthenticationResponse processOAuthGoogleLogin(String code);
}
