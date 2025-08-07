package com.example.RestaurantOS.services.impl;

import com.example.RestaurantOS.enums.MenuCategory;
import com.example.RestaurantOS.enums.OrderItemStatus;
import com.example.RestaurantOS.models.dto.MenuItemDTO;
import com.example.RestaurantOS.models.dto.OrderItemDTO;
import com.example.RestaurantOS.models.entity.MenuItem;
import com.example.RestaurantOS.models.entity.Order;
import com.example.RestaurantOS.models.entity.OrderItem;
import com.example.RestaurantOS.repositories.MenuItemRepository;
import com.example.RestaurantOS.repositories.OrderItemRepository;
import com.example.RestaurantOS.repositories.OrderRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional
    public OrderItemDTO save(OrderItemDTO orderItemDTO) throws ChangeSetPersister.NotFoundException {
        OrderItem orderItem = modelMapper.map(orderItemDTO, OrderItem.class);
        orderItem.setOrderItemStatus(OrderItemStatus.WAITING);
        MenuItem menuItem = menuItemRepository.findById(orderItemDTO.getMenuItemId()).orElseThrow(ChangeSetPersister.NotFoundException::new);
        orderItem.setMenuItem(menuItem);
        orderItem.setName(menuItem.getName());
        orderItem.setPrice(menuItem.getPrice() * orderItem.getQuantity());
        Order order = orderRepository.findById(orderItemDTO.getOrderId()).orElseThrow(ChangeSetPersister.NotFoundException::new);
        orderItem.setOrder(order);
        return modelMapper.map(orderItemRepository.save(orderItem), OrderItemDTO.class);
    }

    public OrderItemDTO findById(Long id) throws ChangeSetPersister.NotFoundException {
        OrderItem orderItem = orderItemRepository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        return modelMapper.map(orderItem, OrderItemDTO.class);
    }

    public List<OrderItemDTO> findAll(Long orderId, OrderItemStatus status) {
        List<OrderItem> all = orderItemRepository.findAll();
        return all.stream()
                .filter(item -> (orderId == null || item.getOrder().getId() == orderId) &&
                        (status == null || item.getOrderItemStatus() == status))
                .map(item -> {
                    OrderItemDTO dto = modelMapper.map(item, OrderItemDTO.class);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(Long id, OrderItemDTO orderItemDTO) throws ChangeSetPersister.NotFoundException {
        OrderItem orderItem = orderItemRepository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        if (orderItemDTO.getQuantity() == 0) {
            delete(id);
        } else {
            orderItem.setQuantity(orderItemDTO.getQuantity());
            orderItem.setSpecialInstructions(orderItemDTO.getSpecialInstructions());
            orderItemRepository.save(orderItem);
        }
    }

    @Transactional
    public void delete(Long id) throws ChangeSetPersister.NotFoundException {
        OrderItem orderItem = orderItemRepository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        orderItem.setMenuItem(null);
        orderItem.setOrder(null);
        orderItemRepository.delete(orderItem);
    }

    @Transactional
    public OrderItemDTO acceptOrderItem(Long orderItemId) throws ChangeSetPersister.NotFoundException {

        OrderItem waitingOrderItem = orderItemRepository.findById(orderItemId).orElseThrow(ChangeSetPersister.NotFoundException::new);

        if (!waitingOrderItem.getOrderItemStatus().equals(OrderItemStatus.WAITING)) {
            throw new ValidationException("Status is already updated");
        }
        waitingOrderItem.setOrderItemStatus(OrderItemStatus.PENDING);
        return modelMapper.map(orderItemRepository.save(waitingOrderItem), OrderItemDTO.class);
    }

}
