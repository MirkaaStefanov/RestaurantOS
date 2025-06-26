package com.example.RestaurantOS.controllers;

import com.example.RestaurantOS.enums.MenuCategory;
import com.example.RestaurantOS.models.dto.MenuItemDTO;
import com.example.RestaurantOS.services.impl.MenuItemService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping
    public ResponseEntity<List<MenuItemDTO>> getAllMenuItems(
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) MenuCategory category,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return ResponseEntity.ok(menuItemService.findAll(available, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemDTO> getById(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String auth) {
        return ResponseEntity.ok(menuItemService.findById(id));
    }

    @PostMapping
    public ResponseEntity<MenuItemDTO> create(@RequestBody MenuItemDTO dto,  @RequestHeader(value = "Authorization", required = false) String auth) {
        return ResponseEntity.ok(menuItemService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemDTO> update(@PathVariable Long id, @RequestBody MenuItemDTO dto,  @RequestHeader(value = "Authorization", required = false) String auth) {
        return ResponseEntity.ok(menuItemService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,  @RequestHeader(value = "Authorization", required = false) String auth) {
        menuItemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<MenuItemDTO> toggleAvailability(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String auth) {
        return ResponseEntity.ok(menuItemService.toggleAvailability(id));
    }
}

