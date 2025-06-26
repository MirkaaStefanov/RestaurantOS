package com.example.RestaurantOS.repositories;

import com.example.RestaurantOS.models.entity.Order;
import com.example.RestaurantOS.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findTop10ByOrderByOrderTimeDesc();

}
