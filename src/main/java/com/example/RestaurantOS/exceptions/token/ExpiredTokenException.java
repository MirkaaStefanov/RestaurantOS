package com.example.RestaurantOS.exceptions.token;


import com.example.RestaurantOS.exceptions.common.UnauthorizedException;

public class ExpiredTokenException extends UnauthorizedException {
    public ExpiredTokenException() {
        super("Токенът е изтекъл!");
    }
}
