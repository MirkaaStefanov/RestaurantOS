package com.example.RestaurantOS.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderItemStatus {
    WAITING,
    PENDING,
    PREPARING,
    DONE

}
