package com.example.RestaurantOS.models.dto.auth;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenBodyDTO {
    private String refreshToken;
}
