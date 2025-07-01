package com.example.RestaurantOS.services.impl;

import com.example.RestaurantOS.enums.TableStatus;
import com.example.RestaurantOS.models.dto.MenuItemDTO;
import com.example.RestaurantOS.models.dto.OrderDTO;
import com.example.RestaurantOS.models.dto.TableDTO;
import com.example.RestaurantOS.models.dto.auth.PublicUserDTO;
import com.example.RestaurantOS.models.entity.Order;
import com.example.RestaurantOS.models.entity.Table;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.repositories.OrderRepository;
import com.example.RestaurantOS.repositories.TableRepository;
import com.example.RestaurantOS.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final ModelMapper modelMapper;
    private final UserServiceImpl userService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public TableDTO save(TableDTO tableDTO) {
        Table table = modelMapper.map(tableDTO, Table.class);
        return modelMapper.map(tableRepository.save(table), TableDTO.class);
    }

    public List<TableDTO> getAll() {
        List<Table> tables = tableRepository.findAll();
        return tables.stream()
                .map(table -> {
                    TableDTO dto = modelMapper.map(table, TableDTO.class);
                    return dto;
                }).collect(Collectors.toList());
    }

    public List<TableDTO> getWaitersTable() throws ChangeSetPersister.NotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User authenticatedUser = userRepository.findByEmail(email).orElseThrow(ChangeSetPersister.NotFoundException::new);

        List<Table> tables = tableRepository.findByWaiter(authenticatedUser);
        return tables.stream()
                .map(table -> {
                    TableDTO dto = modelMapper.map(table, TableDTO.class);
                    return dto;
                }).collect(Collectors.toList());
    }

    public TableDTO findById(UUID id) throws ChangeSetPersister.NotFoundException {
        Table table = tableRepository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        return modelMapper.map(table, TableDTO.class);
    }

    public TableDTO update(UUID id, TableDTO tableDTO) {
        Table table = modelMapper.map(tableDTO, Table.class);
        table.setId(id);
        return modelMapper.map(tableRepository.save(table), TableDTO.class);
    }

    public void delete(UUID id) throws ChangeSetPersister.NotFoundException {
        Table table = tableRepository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        tableRepository.delete(table);
    }

    @Transactional
    public OrderDTO useTable(UUID tableId) throws ChangeSetPersister.NotFoundException { // Method returns OrderDTO
        Table table = tableRepository.findById(tableId).orElseThrow(ChangeSetPersister.NotFoundException::new);


        // If table is already occupied and has an order, return that order's DTO.
        if (table.getStatus() == TableStatus.OCCUPIED && table.getOrder() != null) {
            System.out.println("Table " + table.getNumber() + " is already occupied with Order ID: " + table.getOrder().getId() + ". Returning existing order DTO.");
            return convertToOrderDto(table.getOrder()); // Convert existing order to DTO
        }


//        PublicUserDTO waiter = userService.findMe("token");

        // Create a new order
        Order newOrder = new Order();
//        newOrder.setUser(waiter);
        newOrder.setTable(table);
        newOrder.setOrderTime(LocalDateTime.now());
        newOrder.setTotalAmount(0.0); // Initialize total amount as 0.0 (Double)
        newOrder.setItems(new ArrayList<>()); // CRITICAL FIX: Initialize items as an empty ArrayList

        Order savedOrder = orderRepository.save(newOrder);

        // Link the newly created order and waiter to the table entity
        table.setOrder(savedOrder);
//        table.setWaiter(waiter);
        table.setStatus(TableStatus.OCCUPIED);

        tableRepository.save(table); // Save the table to persist order and waiter linkage

        return convertToOrderDto(savedOrder); // Convert new order to DTO and return
    }


//    // --- Private Helper Method for consistent DTO mapping for Table ---
//    private TableDTO convertToTableDto(Table table) {
//        TableDTO dto = modelMapper.map(table, TableDTO.class);
//        dto.setCurrentOrder(table.getOrder() != null ? table.getOrder().getId() : null);
//        dto.setWaiter(table.getWaiter() != null ? table.getWaiter().getId() : null);
//        return dto;
//    }

    // --- Private Helper Method for consistent DTO mapping for Order ---
    private OrderDTO convertToOrderDto(Order order) {
        OrderDTO dto = modelMapper.map(order, OrderDTO.class);
        // ModelMapper typically handles null collections, but this explicitly ensures it's an ArrayList
        if (dto.getItems() == null) {
            dto.setItems(new ArrayList<>());
        }
        // Manually map fields that ModelMapper might miss due to relationships, if necessary
        // For example, if OrderDTO needs table number or waiter name from related entities
        dto.setTable(order.getTable() != null ? order.getTable().getId() : null);
        dto.setWaiterId(order.getUser() != null ? order.getUser().getId() : null);
        // If ModelMapper is not configured to map nested collections automatically, you'd do:
        // dto.setItems(order.getItems().stream().map(item -> modelMapper.map(item, OrderItemDTO.class)).collect(Collectors.toList()));
        return dto;
    }
}


