package com.example.RestaurantOS.exceptions.token;


import com.example.RestaurantOS.exceptions.common.UnauthorizedException;

/**
 * Exception thrown when the provided authentication token is invalid or expired (so the request cannot be authorized).
 * Sets the appropriate message using MessageSource (the messages are in src/main/resources/messages).
 */
public class InvalidTokenException extends UnauthorizedException {
    public InvalidTokenException() {
        super("Невалиден токен!");
    }
}
