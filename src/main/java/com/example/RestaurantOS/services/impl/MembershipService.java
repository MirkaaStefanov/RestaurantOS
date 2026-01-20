package com.example.RestaurantOS.services.impl;

import com.example.RestaurantOS.models.entity.Membership;
import com.example.RestaurantOS.models.entity.MembershipPlan;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.repositories.DailyWorkoutRepository;
import com.example.RestaurantOS.repositories.MembershipPlanRepository;
import com.example.RestaurantOS.repositories.MembershipRepository;
import com.example.RestaurantOS.repositories.UserRepository;
import com.example.RestaurantOS.repositories.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipPlanRepository planRepository;
    private final UserRepository userRepository;
    private final DailyWorkoutRepository dailyWorkoutRepository;

    @Transactional
    public Membership userPlans(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Membership membership = user.getCurrentMembership();

        if (membership != null) {

            boolean strictlyValid = membership.isValid();

            boolean hasWorkoutToday = dailyWorkoutRepository
                    .findByUserAndDate(user, LocalDate.now())
                    .isPresent();

            boolean isDateValid = membership.getEndDate() == null ||
                    !LocalDate.now().isAfter(membership.getEndDate());

            if (strictlyValid || (hasWorkoutToday && isDateValid)) {
                membership.setDailyAccessActive(true);
            } else {
                membership.setDailyAccessActive(false);
            }
        }

        return membership;
    }
    @Transactional
    public Membership buyMembership(UUID userId, Long planId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Membership currentMembership = user.getCurrentMembership();

        // ПОПРАВКА 1: Проверяваме за NULL преди да викаме .isValid()
        // Ако currentMembership е null, значи няма карта и проверката минава.
        if (currentMembership != null && currentMembership.isValid()) {
            throw new IllegalStateException("Вече имате активна карта! Не може да закупите нова.");
        }

        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Plan ID"));

        // Създаваме новата карта
        Membership newMembership = Membership.builder()
                .owner(user)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(plan.getDurationDays()))
                // .valid(true) -> Това не е нужно, защото @PrePersist ще го сметне автоматично
                .remainingVisits(plan.getVisitLimit()) // Увери се, че в плана е -1 или число
                .build();

        // ПОПРАВКА 2: Обвиваме го в ArrayList, за да може да се променя после
        newMembership.setUsers(new ArrayList<>());

        // Записваме първо картата, за да получи ID
        Membership savedMembership = membershipRepository.save(newMembership);

        // Свързваме потребителя с новата карта
        user.setCurrentMembership(savedMembership);
        userRepository.save(user);

        return savedMembership;
    }
}
