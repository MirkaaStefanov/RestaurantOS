package com.example.RestaurantOS.services.impl;

import com.example.RestaurantOS.enums.OrderItemStatus;
import com.example.RestaurantOS.models.dto.OrderItemDTO;
import com.example.RestaurantOS.models.entity.MenuItem;
import com.example.RestaurantOS.models.entity.Order;
import com.example.RestaurantOS.models.entity.OrderItem;
import com.example.RestaurantOS.repositories.MenuItemRepository;
import com.example.RestaurantOS.repositories.OrderItemRepository;
import com.example.RestaurantOS.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;

    public OrderItemDTO save(OrderItemDTO orderItemDTO) throws ChangeSetPersister.NotFoundException {
        OrderItem orderItem = modelMapper.map(orderItemDTO, OrderItem.class);
        orderItem.setOrderItemStatus(OrderItemStatus.WAITING);
        MenuItem menuItem = menuItemRepository.findById(orderItemDTO.getMenuItemId()).orElseThrow(ChangeSetPersister.NotFoundException::new);
        orderItem.setMenuItem(menuItem);
        Order order = orderRepository.findById(orderItemDTO.getOrderId()).orElseThrow(ChangeSetPersister.NotFoundException::new);
        orderItem.setOrder(order);
        return modelMapper.map(orderItemRepository.save(orderItem), OrderItemDTO.class);
    }
}
