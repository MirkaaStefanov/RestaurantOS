package com.example.RestaurantOS.models.dto;

import com.example.RestaurantOS.models.entity.OrderItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO {
    public Long id;
    private List<OrderItemDTO> items;
    private double totalAmount;
    private UUID waiterId;
    private LocalDateTime orderTime;
    private LocalDateTime estimatedReadyTime;
    private UUID table;
}
