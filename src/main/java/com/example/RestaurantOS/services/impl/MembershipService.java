package com.example.RestaurantOS.services.impl;

import com.example.RestaurantOS.models.entity.Membership;
import com.example.RestaurantOS.models.entity.MembershipPlan;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.repositories.MembershipPlanRepository;
import com.example.RestaurantOS.repositories.MembershipRepository;
import com.example.RestaurantOS.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Transactional
    public Membership userPlans(UUID userId) {
        // ГРЕШКА 1: getById връща Proxy (незареден обект).
        // Ако се опиташ да достъпиш полетата му извън транзакция, гърми.
        // ПОПРАВКА: Използвай findById, което вади истинския обект.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Membership membership = user.getCurrentMembership();

        // ГРЕШКА 2: Lazy Loading.
        // Ако membership не е null, списъкът users вътре в него е Lazy.
        // Трябва да го "събудим", докато сме още в транзакцията.
//        if (membership != null) {
//            Hibernate.initialize(membership.getUsers());
//            Hibernate.initialize(membership.getOwner());
//        }

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
