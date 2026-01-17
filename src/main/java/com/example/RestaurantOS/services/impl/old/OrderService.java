package com.example.RestaurantOS.services.impl.old;

import com.example.RestaurantOS.enums.Role;
import com.example.RestaurantOS.models.dto.old.OrderDTO;
import com.example.RestaurantOS.models.entity.old.Order;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.repositories.old.OrderRepository;
import com.example.RestaurantOS.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    public List<OrderDTO> findAll() throws ChangeSetPersister.NotFoundException {
        List<Order> orders;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User authenticateUser = userRepository.findByEmail(email).orElseThrow(ChangeSetPersister.NotFoundException::new);


        if (authenticateUser.getRole().equals(Role.ADMIN)) {
            orders = orderRepository.findAll();
        } else {
            orders = orderRepository.findByUser(authenticateUser);
        }

        return orders.stream()
                .map(order -> {
                    OrderDTO dto = mapper.map(order, OrderDTO.class);
                    // Map image bytes etc if needed inside OrderDTO mapper
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public OrderDTO findById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow();
        return mapper.map(order, OrderDTO.class);
    }

    public OrderDTO save(OrderDTO dto) {
        Order order = mapper.map(dto, Order.class);
        Order savedOrder = orderRepository.save(order);
        OrderDTO savedOrderDTO = mapper.map(savedOrder, OrderDTO.class);
        messagingTemplate.convertAndSend("/topic/orders", savedOrderDTO);

        return savedOrderDTO;
    }

    public OrderDTO update(Long id, OrderDTO dto) throws ChangeSetPersister.NotFoundException {
        Order order = orderRepository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        Order updatedOrder = orderRepository.save(order);
        OrderDTO updatedOrderDTO = mapper.map(updatedOrder, OrderDTO.class);

        messagingTemplate.convertAndSend("/topic/orders", updatedOrderDTO);

        return updatedOrderDTO;
    }

    public void delete(Long id) throws ChangeSetPersister.NotFoundException {
        Order order = orderRepository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        orderRepository.delete(order);
        messagingTemplate.convertAndSend("/topic/orders/deleted", id);
    }


    public List<OrderDTO> getRecentOrders() {
        List<Order> recentOrders = orderRepository.findTop10ByOrderByOrderTimeDesc();
        return recentOrders.stream()
                .map(order -> mapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

}
