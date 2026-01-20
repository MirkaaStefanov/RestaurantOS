package com.example.RestaurantOS.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime; // <--- Добави този импорт

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "daily_workouts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "date"})
})
public class DailyWorkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private User user;

    private LocalDate date;      // Дата: 2026-01-18

    // НОВО ПОЛЕ: Час на първо влизане
    private LocalTime startTime;// Час: 10:30:15

    private LocalTime reStartTime;

    @Column(columnDefinition = "TEXT")
    private String userNote;

    private Integer rating;

    private LocalDateTime endTime;


}