package com.example.RestaurantOS.repositories;

import com.example.RestaurantOS.models.entity.Token;
import com.example.RestaurantOS.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findAllByUser(User user);

    Optional<Token> findByToken(String token);
}
