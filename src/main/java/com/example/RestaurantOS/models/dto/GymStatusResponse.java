package com.example.RestaurantOS.models.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;

@Data
@Builder
public class GymStatusResponse {
    private boolean inGym;       // true = покажи бутон "Приключи"
    private LocalTime startTime; // За да показваш "Тренирате от 10:30"
    private Long workoutId;      // За да знаеш кое ID да приключиш
}