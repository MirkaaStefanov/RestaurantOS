package com.example.RestaurantOS.config;

import com.example.RestaurantOS.filters.JwtAuthenticationFilter;
import com.example.RestaurantOS.handlers.JwtAuthenticationEntryPoint;
import com.example.RestaurantOS.security.WebOAuth2SuccessHandler; // Увери се, че този импорт съществува
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private final ObjectMapper objectMapper;

    // Този handler е нужен за Web OAuth2 (Thymeleaf) логина
    private final WebOAuth2SuccessHandler webOAuth2SuccessHandler;

    // --- 1. CONFIGURATION FOR REST API (MOBILE APP) ---
    // Този филтър се изпълнява първи и хваща само /api/** заявки
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable) // За REST API не ни трябва CSRF
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT е stateless
                .exceptionHandling(customizer -> {
                    // Връща JSON грешка (401), вместо да препраща към login.html
                    customizer.authenticationEntryPoint(new JwtAuthenticationEntryPoint(objectMapper));
                })
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**", "/api/v1/oauth2/**").permitAll()
                        .requestMatchers("/api/v1/tables").hasAnyRole("ADMIN", "WAITER")
                        // Можеш да добавиш още специфични правила за API тук
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                );

        return http.build();
    }

    // --- 2. CONFIGURATION FOR WEB (THYMELEAF) ---
    // Изпълнява се, ако URL-ът НЕ започва с /api/. Тук пазим сесии.
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**") // Хваща всичко останало (Web UI)
                .csrf(Customizer.withDefaults()) // За формите е добре да има CSRF защита
                .authorizeHttpRequests(auth -> auth
                        // Разрешаваме достъп до статични ресурси и login/register страниците
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .anyRequest().authenticated()
                )
                // --- Standard Form Login (Email/Pass) ---
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                // --- OAuth2 Login (Google за Web) ---
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // Използваме същата логин страница
                        .successHandler(webOAuth2SuccessHandler) // ТУК е магията: свързва Google акаунта с DB и пуска потребителя
                )
                // --- Logout за Web ---
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .authenticationProvider(authenticationProvider);

        return http.build();
    }
}