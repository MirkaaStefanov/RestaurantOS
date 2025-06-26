package com.example.RestaurantOS.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TableStatus {

    AVAILABLE,
    OCCUPIED,
    RESERVED,
    NEEDS_CLEANING
}
