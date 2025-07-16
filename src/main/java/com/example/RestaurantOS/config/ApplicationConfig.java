package com.example.RestaurantOS.config;


import com.example.RestaurantOS.exceptions.user.UserNotFoundException;
import com.example.RestaurantOS.models.baseEntity.BaseEntity;
import com.example.RestaurantOS.models.dto.MenuItemDTO;
import com.example.RestaurantOS.models.dto.OrderDTO;
import com.example.RestaurantOS.models.dto.common.BaseDTO;
import com.example.RestaurantOS.models.entity.MenuItem;
import com.example.RestaurantOS.models.entity.Order;
import com.example.RestaurantOS.models.entity.Table;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.repositories.TableRepository;
import com.example.RestaurantOS.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Configuration class for defining beans related to application setup, such as ModelMapper, ObjectMapper,
 * UserDetailsService, AuthenticationProvider, AuthenticationManager, PasswordEncoder, and RestTemplate.
 */
@Configuration
@RequiredArgsConstructor
@EnableAspectJAutoProxy
@EnableAsync
public class ApplicationConfig {
    private final UserRepository repository;
    private final TableRepository tableRepository;

    @Bean
    @Primary
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Global configuration
        configureGlobalSettings(modelMapper);

        // Product-specific mappings
        configureMenuItemMappings(modelMapper);

        return modelMapper;
    }

    private void configureGlobalSettings(ModelMapper modelMapper) {
        modelMapper.getConfiguration()
                .setPropertyCondition(context -> {
                    if (context.getParent() != null &&
                            context.getParent().getDestination() instanceof BaseEntity &&
                            context.getParent().getSource() instanceof BaseDTO) {

                        String destinationProperty = context.getMapping().getLastDestinationProperty().getName();
                        return !("id".equals(destinationProperty) ||
                                "createdAt".equals(destinationProperty) ||
                                "updatedAt".equals(destinationProperty) ||
                                "deletedAt".equals(destinationProperty));
                    }
                    return true;
                })
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true);
    }

    private void configureMenuItemMappings(ModelMapper modelMapper) {
        Converter<byte[], String> toBase64 = context -> {
            byte[] source = context.getSource();
            return (source != null) ? Base64.getEncoder().encodeToString(source) : null;
        };

        modelMapper.createTypeMap(MenuItem.class, MenuItemDTO.class)
                .addMappings(mapper -> mapper.using(toBase64)
                        .map(MenuItem::getImageData, MenuItemDTO::setImage));
    }
    private void configureOrderMappings(ModelMapper modelMapper) {
        // --- Order (Model) to OrderDTO (DTO) Mappings ---
        modelMapper.createTypeMap(Order.class, OrderDTO.class)
                .addMappings(mapper -> {
                    // Map User (entity) ID to waiterId (UUID in DTO)
                    mapper.map(src -> src.getUser().getId(), OrderDTO::setWaiterId);
                    // Map Table (entity) ID to table (UUID in DTO)
                    mapper.map(src -> src.getTable().getId(), OrderDTO::setTable);
                    // 'items', 'totalAmount', 'orderTime', 'estimatedReadyTime' will be mapped automatically
                    // if OrderItem and OrderItemDTO have compatible field names.
                });

        // --- OrderDTO (DTO) to Order (Model) Mappings ---
        modelMapper.createTypeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> {
                    // Convert waiterId (UUID) to User entity
                    Converter<UUID, User> uuidToUserConverter = context -> {
                        UUID userId = context.getSource();
                        if (userId == null) {
                            return null;
                        }
                        // Fetch the User entity from the database using the UserRepository
                        return repository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException());
                    };
                    mapper.using(uuidToUserConverter).map(OrderDTO::getWaiterId, Order::setUser);

                    // Convert table (UUID) to Table entity
                    Converter<UUID, Table> uuidToTableConverter = context -> {
                        UUID tableId = context.getSource();
                        if (tableId == null) {
                            return null;
                        }
                        // Fetch the Table entity from the database using the TableRepository
                        return tableRepository.findById(tableId)
                                .orElseThrow(() -> new RuntimeException("Table with ID " + tableId + " not found during mapping."));
                    };
                    mapper.using(uuidToTableConverter).map(OrderDTO::getTable, Order::setTable);
                });
    }


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return objectMapper;
    }

    @Bean
    public Validator validator() {
        Validator validator;

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        return validator;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repository.findByEmail(username)
                .orElseThrow(UserNotFoundException::new);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
