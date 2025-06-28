package com.example.RestaurantOS.services.impl;

import com.example.RestaurantOS.enums.OrderItemStatus;
import com.example.RestaurantOS.enums.Role;
import com.example.RestaurantOS.enums.TableStatus;
import com.example.RestaurantOS.models.dto.OrderDTO;
import com.example.RestaurantOS.models.dto.OrderItemDTO;
import com.example.RestaurantOS.models.entity.MenuItem;
import com.example.RestaurantOS.models.entity.Order;
import com.example.RestaurantOS.models.entity.OrderItem;
import com.example.RestaurantOS.models.entity.Table;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.repositories.MenuItemRepository;
import com.example.RestaurantOS.repositories.OrderRepository;
import com.example.RestaurantOS.repositories.TableRepository;
import com.example.RestaurantOS.repositories.UserRepository;
import com.example.RestaurantOS.services.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository; // To get User (waiter) by ID
    private final TableRepository tableRepository;
    private final UserServiceImpl userService;

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getRecentOrders(int limit) {
        return orderRepository.findAllByOrderByOrderTimeDesc(PageRequest.of(0, limit)).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) throws ChangeSetPersister.NotFoundException {
        Order order = orderRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
        return convertToDto(order);
    }

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) throws ChangeSetPersister.NotFoundException { // tableId removed from method signature, now in orderDTO
        Order order = new Order();
        order.setOrderTime(LocalDateTime.now());
        order.setTotalAmount(0);

        User user = userService.findMe();

        order.setUser(user);
        if (orderDTO.getTableId() != null) {
            Table table = tableRepository.findById(orderDTO.getTableId())
                    .orElseThrow(ChangeSetPersister.NotFoundException::new);
            order.setTable(table);
        } else {
            throw new IllegalArgumentException("Table ID is required for new order.");
        }

        if (orderDTO.getItems() != null && !orderDTO.getItems().isEmpty()) {
            double calculatedTotal = 0;
            for (OrderItemDTO itemDTO : orderDTO.getItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);

                if (itemDTO.getMenuItemId() != null) {
                    MenuItem menuItem = menuItemRepository.findById(itemDTO.getMenuItemId())
                            .orElseThrow(ChangeSetPersister.NotFoundException::new);
                    orderItem.setMenuItem(menuItem);
                    orderItem.setName(menuItem.getName()); // Copy name/price from menu item
                    orderItem.setPrice(menuItem.getPrice()* itemDTO.getQuantity());
                } else {
                    // Allow custom item if menuItemId is not provided (should also provide name and price)
                    if (itemDTO.getName() == null || itemDTO.getPrice() == 0) {
                        throw new IllegalArgumentException("Custom order item must have a name and price.");
                    }
                    orderItem.setName(itemDTO.getName());
                    orderItem.setPrice(itemDTO.getPrice()* itemDTO.getQuantity());
                }

                orderItem.setQuantity(itemDTO.getQuantity());
                orderItem.setSpecialInstructions(itemDTO.getSpecialInstructions());
                orderItem.setOrderItemStatus(itemDTO.getOrderItemStatus() != null ? itemDTO.getOrderItemStatus() : OrderItemStatus.PENDING);

                order.getItems().add(orderItem);
                calculatedTotal += orderItem.getPrice() * orderItem.getQuantity();
            }
            order.setTotalAmount(calculatedTotal);
        }

        Order savedOrder = orderRepository.save(order);

        // Crucial: Link the new order to the table's currentOrder field
        Table table = savedOrder.getTable();
        if (table != null) {
            table.setOrder(savedOrder);
            table.setStatus(TableStatus.OCCUPIED); // Table becomes occupied
            tableRepository.save(table);
        }

        return convertToDto(savedOrder);
    }

    @Transactional
    public OrderDTO updateOrder(Long id, OrderDTO orderDTO) throws ChangeSetPersister.NotFoundException {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);

        // Update total amount if items are being updated
        double newTotal = 0;
        // Handle OrderItems update: This is a robust way to add/update/remove items
        if (orderDTO.getItems() != null) {
            // Map new DTO items to existing OrderItems or create new ones
            List<OrderItem> updatedOrderItems = orderDTO.getItems().stream()
                    .map(itemDTO -> {
                        OrderItem orderItem = null;
                        if (itemDTO.getId() != null) { // Try to find existing item
                            Optional<OrderItem> existingOrderItemOpt = existingOrder.getItems().stream()
                                    .filter(oi -> oi.getId().equals(itemDTO.getId()))
                                    .findFirst();
                            if (existingOrderItemOpt.isPresent()) {
                                orderItem = existingOrderItemOpt.get();
                            }
                        }
                        // If no existing item or new item, create new OrderItem
                        if (orderItem == null) {
                            orderItem = new OrderItem();
                            orderItem.setOrder(existingOrder); // Link to parent order
                        }

                        // Update properties
                        if (itemDTO.getMenuItemId() != null) {
                            MenuItem menuItem = menuItemRepository.findById(itemDTO.getMenuItemId()).orElseThrow(ChangeSetPersister.NotFoundException::new);
                            orderItem.setMenuItem(menuItem);
                            orderItem.setName(menuItem.getName());
                            orderItem.setPrice(menuItem.getPrice());
                        } else { // Handle custom item updates
                            if (itemDTO.getName() == null || itemDTO.getPrice() == 0) {
                                throw new IllegalArgumentException("Custom order item must have a name and price.");
                            }
                            orderItem.setName(itemDTO.getName());
                            orderItem.setPrice(itemDTO.getPrice());
                        }
                        orderItem.setQuantity(itemDTO.getQuantity());
                        orderItem.setSpecialInstructions(itemDTO.getSpecialInstructions());
                        orderItem.setOrderItemStatus(itemDTO.getOrderItemStatus() != null ? itemDTO.getOrderItemStatus() : OrderItemStatus.PENDING);
                        return orderItem;
                    })
                    .collect(Collectors.toList());

            // Remove items that are no longer in the updated list
            existingOrder.getItems().removeIf(oldItem -> updatedOrderItems.stream().noneMatch(newItem -> oldItem.getId().equals(newItem.getId())));
            // Add/update items
            updatedOrderItems.forEach(updatedItem -> {
                if (!existingOrder.getItems().contains(updatedItem)) {
                    existingOrder.getItems().add(updatedItem);
                }
            });

            newTotal = existingOrder.getItems().stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
            existingOrder.setTotalAmount(newTotal);
        } else {
            // If items array is null, means no item update, maintain current total
            newTotal = existingOrder.getItems().stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
            existingOrder.setTotalAmount(newTotal);
        }


        // Update estimated ready time
        if (orderDTO.getEstimatedReadyTime() != null) existingOrder.setEstimatedReadyTime(orderDTO.getEstimatedReadyTime());

        // --- No orderStatus field to manage as per your request ---

        return convertToDto(orderRepository.save(existingOrder));
    }

    @Transactional
    public void deleteOrder(Long id) throws ChangeSetPersister.NotFoundException {
        Order order = orderRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);

        // If order is linked to a table's currentOrder, unlink it first
        Table table = order.getTable(); // Get the table associated with this order
        if (table != null && table.getOrder() != null && table.getOrder().getId().equals(id)) {
            table.setOrder(null); // Unlink the current order from the table
            // Decide new table status after order is deleted/completed
            table.setStatus(TableStatus.AVAILABLE); // Or DIRTY, based on business logic
            tableRepository.save(table);
        }
        orderRepository.delete(order);
    }

    // --- Mappers ---
    private OrderDTO convertToDto(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::convertOrderItemToDto)
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .items(itemDTOs)
                .totalAmount(order.getTotalAmount())
                .waiterId(order.getUser() != null ? order.getUser().getId() : null) // Assuming User has getId()
                .tableId(order.getTable() != null ? order.getTable().getId() : null) // Get linked table ID
                .orderTime(order.getOrderTime())
                .estimatedReadyTime(order.getEstimatedReadyTime())
                .build();
    }

    private OrderItemDTO convertOrderItemToDto(OrderItem orderItem) {
        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .menuItemId(orderItem.getMenuItem() != null ? orderItem.getMenuItem().getId() : null)
                .name(orderItem.getName())
                .price(orderItem.getPrice())
                .quantity(orderItem.getQuantity())
                .specialInstructions(orderItem.getSpecialInstructions())
                .orderItemStatus(orderItem.getOrderItemStatus())
                .build();
    }
}
