package com.example.RestaurantOS.repositories;

import com.example.RestaurantOS.models.entity.DailyWorkout;
import com.example.RestaurantOS.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyWorkoutRepository extends JpaRepository<DailyWorkout, Long> {

    // Търсим дали вече има тренировка за днес
    Optional<DailyWorkout> findByUserAndDate(User user, LocalDate date);

    long countByDateAndEndTimeIsNull(LocalDate date);

    Optional<DailyWorkout> findByUserAndDateAndEndTimeIsNull(User user, LocalDate date);

    List<DailyWorkout> findAllByUserOrderByDateDesc(User user);
}