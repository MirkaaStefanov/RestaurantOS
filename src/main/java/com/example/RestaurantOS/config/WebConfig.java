package com.example.RestaurantOS.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

// Configuration class for web-related configurations
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final FrontendConfig frontendConfig;



    // Method to configure CORS mappings
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**") // Allow CORS for all endpoints
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Allowed HTTP methods
                .allowedOrigins(frontendConfig.getBaseUrl(),
                        "http://localhost:19000",          // For Expo web preview
                        "http://127.0.0.1:19000",         // Common for some local dev setups
                        "exp://192.168.0.13:19000",// IMPORTANT: Replace YOUR_MAC_IP_ADDRESS
                        "http://192.168.0.13:19000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*") // Allowed origins (frontend URL)
                .allowCredentials(true); // Allow credentials (cookies, authorization headers)
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }
}

