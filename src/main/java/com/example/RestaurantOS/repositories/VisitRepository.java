package com.example.RestaurantOS.repositories;

import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.models.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.nio.file.LinkOption;
import java.time.LocalDateTime;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    boolean existsByUserAndEntryTimeBetween(User user, LocalDateTime start, LocalDateTime end);

}
