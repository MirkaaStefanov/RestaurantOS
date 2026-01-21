package com.example.RestaurantOS.repositories;

import com.example.RestaurantOS.models.dto.OrderItemDTO;
import com.example.RestaurantOS.models.entity.Order;
import com.example.RestaurantOS.models.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

}
