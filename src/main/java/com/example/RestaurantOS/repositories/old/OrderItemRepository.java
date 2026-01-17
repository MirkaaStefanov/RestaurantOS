package com.example.RestaurantOS.repositories.old;

import com.example.RestaurantOS.models.entity.old.Order;
import com.example.RestaurantOS.models.entity.old.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

}
