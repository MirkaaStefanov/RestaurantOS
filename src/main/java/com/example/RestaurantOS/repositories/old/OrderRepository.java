package com.example.RestaurantOS.repositories.old;

import com.example.RestaurantOS.models.entity.old.Order;
import com.example.RestaurantOS.models.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findTop10ByOrderByOrderTimeDesc();


    List<Order> findAllByOrderByOrderTimeDesc(PageRequest pageRequest);
}
