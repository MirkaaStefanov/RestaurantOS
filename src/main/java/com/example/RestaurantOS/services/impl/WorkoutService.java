package com.example.RestaurantOS.services.impl;

import com.example.RestaurantOS.models.dto.GymStatusResponse;
import com.example.RestaurantOS.models.entity.DailyWorkout;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.repositories.DailyWorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final DailyWorkoutRepository dailyWorkoutRepository;

    // Метод за приключване на тренировката
    @Transactional
    public void finishWorkout(User user) {
        LocalDate today = LocalDate.now();

        // Търсим активна тренировка за днес, която още не е приключила
        DailyWorkout workout = dailyWorkoutRepository
                .findByUserAndDateAndEndTimeIsNull(user, today)
                .orElseThrow(() -> new IllegalStateException("Нямате активна тренировка за приключване."));

        workout.setEndTime(LocalDateTime.now());
        dailyWorkoutRepository.save(workout);
    }

    public long getPeopleInGym() {
        return dailyWorkoutRepository.countByDateAndEndTimeIsNull(LocalDate.now());
    }

    public GymStatusResponse getMyStatus(User user) {
        LocalDate today = LocalDate.now();

        // Търсим дали има запис за днес
        Optional<DailyWorkout> workoutOpt = dailyWorkoutRepository.findByUserAndDate(user, today);

        if (workoutOpt.isPresent()) {
            DailyWorkout workout = workoutOpt.get();

            // Ако endTime е NULL -> Значи е още в залата!
            if (workout.getEndTime() == null) {

                LocalTime displayTime = (workout.getReStartTime() != null)
                        ? workout.getReStartTime()
                        : workout.getStartTime();

                return GymStatusResponse.builder()
                        .inGym(true)
                        .startTime(displayTime) // <--- Тук подаваме правилния час
                        .workoutId(workout.getId())
                        .build();
            }
        }

        // В противен случай (няма запис или вече е приключил)
        return GymStatusResponse.builder()
                .inGym(false)
                .build();
    }

}