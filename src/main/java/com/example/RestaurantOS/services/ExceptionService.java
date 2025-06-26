package com.example.RestaurantOS.services;


import com.example.RestaurantOS.exceptions.common.ApiException;

public interface ExceptionService {

    void log(ApiException runtimeException);

    void log(RuntimeException runtimeException, int statusCode);
}
