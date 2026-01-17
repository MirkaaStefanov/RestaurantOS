package com.example.RestaurantOS.controllers.web;

import com.example.RestaurantOS.models.entity.MembershipPlan;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.services.impl.MembershipPlanService;
import com.example.RestaurantOS.services.impl.QrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/membership-plans")
@RequiredArgsConstructor
public class MembershipPlanControllerWeb {

    private final MembershipPlanService membershipPlanService;
    private final QrService qrService;

    // --- 1. Покажи формата за добавяне на план ---
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("membershipPlan", new MembershipPlan());
        return "admin/membership-plan-add"; // Thymeleaf template
    }

    // --- 2. Обработи POST request за добавяне ---
    @PostMapping("/add")
    public String addMembershipPlan(
            @ModelAttribute("membershipPlan") @Valid MembershipPlan membershipPlan,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            // Връща формата с грешки
            return "admin/membership-plan-add";
        }

        membershipPlanService.createPlan(membershipPlan);
        model.addAttribute("successMessage", "Membership plan added successfully!");
        return "redirect:/admin/membership-plans/list";
    }

    // --- 3. Списък с всички планове (опционално) ---
    @GetMapping("/list")
    public String listMembershipPlans(Model model) {
        model.addAttribute("plans", membershipPlanService.getAllPlans());
        return "admin/membership-plan-list"; // Thymeleaf template
    }

    @GetMapping("/validate")
    public String validateForm(){
        return "form";
    }

    @PostMapping("/validate")
    public String validate(@RequestParam String token){
        User user = qrService.validateQrToken(token);
        return "success";
    }

}
