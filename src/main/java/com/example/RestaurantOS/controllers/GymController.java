package com.example.RestaurantOS.controllers;

import com.example.RestaurantOS.models.dto.GymStatusResponse;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.services.impl.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/gym")
@RequiredArgsConstructor
public class GymController {

    private final WorkoutService workoutService;

    // 1. Колко хора има в момента? (Публичен endpoint, може и без auth ако е за табло)
    @GetMapping("/traffic")
    public ResponseEntity<?> getTraffic() {
        long count = workoutService.getPeopleInGym();
        return ResponseEntity.ok(Map.of(
                "activeUsers", count,
                "status", count > 50 ? "CROWDED" : "FREE" // Примерна логика
        ));
    }

    // 2. Бутон "Приключи тренировка" (User action)
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@AuthenticationPrincipal User user) {
        try {
            workoutService.finishWorkout(user);
            return ResponseEntity.ok(Map.of("message", "Тренировката приключи успешно!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-status")
    public ResponseEntity<GymStatusResponse> checkMyStatus(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(workoutService.getMyStatus(user));
    }
}
