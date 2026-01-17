package com.example.RestaurantOS.services.impl;

import com.example.RestaurantOS.models.entity.MembershipPlan;
import com.example.RestaurantOS.repositories.MembershipPlanRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipPlanService {

    private final MembershipPlanRepository planRepo;



    public MembershipPlan getPlanById(Long id) {
        return planRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
    }

    public MembershipPlan createPlan(MembershipPlan plan) {
        return planRepo.save(plan);
    }

    public MembershipPlan updatePlan(Long id, MembershipPlan updated) {
        MembershipPlan plan = getPlanById(id);
        plan.setName(updated.getName());
        plan.setDurationDays(updated.getDurationDays());
        plan.setVisitLimit(updated.getVisitLimit());
        plan.setPrice(updated.getPrice());
        return planRepo.save(plan);
    }

    public void deletePlan(Long id) {
        planRepo.deleteById(id);
    }

    public List<MembershipPlan> getAllPlans() {
        return planRepo.findAll();
    }
}

