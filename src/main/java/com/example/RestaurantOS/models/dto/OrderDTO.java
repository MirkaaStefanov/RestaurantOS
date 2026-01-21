package com.example.RestaurantOS.models.dto;

import com.example.RestaurantOS.models.dto.auth.PublicUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO {
    public Long id;
    private double totalAmount;
    private PublicUserDTO user;
    private LocalDateTime orderTime;
    private LocalDateTime estimatedReadyTime;
    private TableDTO table;
}
