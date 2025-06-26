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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {

    public Long id;
    private MenuItemDTO menuItem;
    private String name;
    private double price;
    private int quantity;
    private String specialInstructions;
    private OrderItemStatus orderItemStatus;
    private OrderDTO order;

}
