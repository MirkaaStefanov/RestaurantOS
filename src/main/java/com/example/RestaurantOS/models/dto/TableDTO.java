package com.example.RestaurantOS.models.dto;

import com.example.RestaurantOS.enums.TableStatus;
import com.example.RestaurantOS.models.dto.auth.PublicUserDTO;
import com.example.RestaurantOS.models.entity.Order;
import com.example.RestaurantOS.models.entity.User;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableDTO {

    public UUID id;
    private int number;
    private int capacity;
    private TableStatus status;
    private OrderDTO currentOrder;
    private PublicUserDTO waiter;
}
