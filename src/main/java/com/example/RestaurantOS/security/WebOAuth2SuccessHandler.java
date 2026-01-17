package com.example.RestaurantOS.security;

import com.example.RestaurantOS.enums.Provider;
import com.example.RestaurantOS.enums.Role;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class WebOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // 1. Проверяваме дали потребителят съществува
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // 2. Ако е нов, го регистрираме
            user = new User();
            user.setEmail(email);

            // Google дава имената по различен начин понякога
            String name = oAuth2User.getAttribute("given_name");
            String surname = oAuth2User.getAttribute("family_name");

            // Fallback ако няма фамилия
            if (name == null) name = oAuth2User.getAttribute("name");
            if (surname == null) surname = "";

            user.setName(name != null ? name : "User");
            user.setSurname(surname);
            user.setRole(Role.USER);
            user.setProvider(Provider.GOOGLE);
            user.setEnabled(true);
            user.setPassword(""); // Няма парола, защото влиза с Google

            userRepository.save(user);
        } else {
            // 3. Ако съществува, обновяваме Provider-а, ако е нужно
            if (user.getProvider() == Provider.LOCAL) {
                user.setProvider(Provider.GOOGLE);
                userRepository.save(user);
            }
        }

        // 4. Пренасочваме към /home
        setDefaultTargetUrl("/home");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}