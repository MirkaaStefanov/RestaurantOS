package com.example.RestaurantOS.models.dto;

import com.example.RestaurantOS.enums.OrderItemStatus;
import com.example.RestaurantOS.models.entity.MenuItem;
import com.example.RestaurantOS.models.entity.Order;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDTO {

    public Long id;
    private Long menuItemId;
    private String name;
    private double price;
    private int quantity;
    private String specialInstructions;
    private OrderItemStatus orderItemStatus;
    private Long orderId;

}
