package com.example.RestaurantOS.repositories;

import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.models.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);

    VerificationToken findByUser(User user);

    List<VerificationToken> findByUserAndCreatedAtBefore(User user, LocalDateTime thresholdDateTime);

    void deleteAllByUser(User user);
}
