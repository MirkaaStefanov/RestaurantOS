package com.example.RestaurantOS.controllers;

import com.example.RestaurantOS.models.entity.MembershipPlan;
import com.example.RestaurantOS.services.impl.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/membership-plans")
public class MembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    @GetMapping
    public ResponseEntity<List<MembershipPlan>> allPlans(){
        return ResponseEntity.ok(membershipPlanService.getAllPlans());
    }
}
