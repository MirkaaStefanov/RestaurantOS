package com.example.RestaurantOS.controllers;

import com.example.RestaurantOS.models.entity.DailyWorkout;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.repositories.DailyWorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final DailyWorkoutRepository dailyWorkoutRepository;

    // 1. Взимане на историята (Показваме DailyWorkout, а не суровите Visits)
    @GetMapping
    public ResponseEntity<List<DailyWorkout>> getMyWorkouts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dailyWorkoutRepository.findAllByUserOrderByDateDesc(user));
    }

    // 2. Добавяне на бележка към тренировка
    @PostMapping("/{workoutId}/note")
    public ResponseEntity<?> updateNote(
            @PathVariable Long workoutId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User user
    ) {
        String note = request.get("note");

        DailyWorkout workout = dailyWorkoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Тренировката не е намерена"));

        // Важно: Проверяваме дали тренировката принадлежи на този потребител!
        if (!workout.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Нямате право да редактирате тази тренировка.");
        }

        workout.setUserNote(note);
        dailyWorkoutRepository.save(workout);

        return ResponseEntity.ok(workout);
    }
}