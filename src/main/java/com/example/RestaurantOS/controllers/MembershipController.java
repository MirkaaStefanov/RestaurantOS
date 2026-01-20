package com.example.RestaurantOS.controllers;

import com.example.RestaurantOS.models.entity.Membership;
import com.example.RestaurantOS.models.entity.MembershipPlan;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.services.impl.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/membership")
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/forUser")
    public ResponseEntity<Membership> userPlans(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(membershipService.userPlans(user.getId()));
    }

    @PostMapping("/buy/{planId}")
    public ResponseEntity<Membership> buyMembership(
            @PathVariable Long planId,
            @AuthenticationPrincipal User user
    ) {
        Membership membership = membershipService.buyMembership(user.getId(), planId);
        return ResponseEntity.ok(membership);
    }

    @PostMapping("/family/add")
    public ResponseEntity<?> addFamilyMember(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User user
    ) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email адресът е задължителен."));
        }

        try {
            // Извикваме новия сървис метод
            Membership updatedMembership = membershipService.addFamilyMember(user.getId(), email);

            return ResponseEntity.ok(updatedMembership);

        } catch (RuntimeException e) {
            // Връщаме грешка (400 Bad Request) с ясно съобщение (напр. "Капацитетът е достигнат")
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
