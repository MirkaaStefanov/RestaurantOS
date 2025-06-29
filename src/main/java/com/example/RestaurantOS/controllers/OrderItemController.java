package com.example.RestaurantOS.controllers;

import com.example.RestaurantOS.enums.MenuCategory;
import com.example.RestaurantOS.enums.OrderItemStatus;
import com.example.RestaurantOS.models.dto.MenuItemDTO;
import com.example.RestaurantOS.models.dto.OrderItemDTO;
import com.example.RestaurantOS.services.impl.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @GetMapping
    public ResponseEntity<List<OrderItemDTO>> getAllOrderItems(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) OrderItemStatus status,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return ResponseEntity.ok(orderItemService.findAll(orderId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItemDTO> getById(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String auth) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(orderItemService.findById(id));
    }

    @PostMapping
    public ResponseEntity<OrderItemDTO> create(@RequestBody OrderItemDTO dto, @RequestHeader(value = "Authorization", required = false) String auth) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(orderItemService.save(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody OrderItemDTO dto, @RequestHeader(value = "Authorization", required = false) String auth) throws ChangeSetPersister.NotFoundException {
        orderItemService.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String auth) throws ChangeSetPersister.NotFoundException {
        orderItemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accept/{id}")
    public ResponseEntity<Void> accept(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String auth) throws ChangeSetPersister.NotFoundException {
        orderItemService.acceptOrder(id);
        return ResponseEntity.noContent().build();
    }

}
