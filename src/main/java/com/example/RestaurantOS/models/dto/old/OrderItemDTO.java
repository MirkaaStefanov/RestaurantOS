package com.example.RestaurantOS.models.dto.old;

import com.example.RestaurantOS.enums.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDTO {

    public Long id;
    private MenuItemDTO menuItem;
    private Long menuItemId;
    private String name;
    private double price;
    private int quantity;
    private String specialInstructions;
    private OrderItemStatus orderItemStatus;
    private OrderDTO order;
    private Long orderId;
    private LocalDateTime addedTime;

}
