package com.example.RestaurantOS.controllers.web;

import com.example.RestaurantOS.models.dto.auth.RegisterRequest;
import com.example.RestaurantOS.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class WebAuthController {

    private final AuthenticationService authenticationService;

    // --- LOGIN PAGE ---
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Търси login.html в templates
    }

    // --- HOME PAGE (След вход) ---
    @GetMapping("/home")
    public String homePage() {
        return "home"; // Търси home.html
    }

    // --- ROOT URL Redirect ---
    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    // --- REGISTER PAGE ---
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register"; // Търси register.html
    }

    // --- REGISTER SUBMIT ---
    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute RegisterRequest request, Model model) {
        try {
            // Използваме същия сървис като за мобилното приложение!
            authenticationService.register(request);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", "Error registering: " + e.getMessage());
            return "register";
        }
    }
}