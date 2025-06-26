package com.example.RestaurantOS.services.impl;

import com.example.RestaurantOS.enums.MenuCategory;
import com.example.RestaurantOS.models.dto.MenuItemDTO;
import com.example.RestaurantOS.models.entity.MenuItem;
import com.example.RestaurantOS.repositories.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository repository;
    private final ModelMapper mapper;

    public List<MenuItemDTO> findAll(Boolean available, MenuCategory category) {
        List<MenuItem> items = repository.findAll();

        return items.stream()
                .filter(item -> (available == null || item.isAvailable() == available) &&
                        (category == null || item.getCategory() == category))
                .map(item -> {
                    MenuItemDTO dto = mapper.map(item, MenuItemDTO.class);
                    dto.setImage(item.getBase64Image());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public MenuItemDTO findById(Long id) {
        MenuItem item = repository.findById(id).orElseThrow();
        MenuItemDTO dto = mapper.map(item, MenuItemDTO.class);
        dto.setImage(item.getBase64Image());
        return dto;
    }

    public MenuItemDTO save(MenuItemDTO dto) {
        MenuItem entity = mapper.map(dto, MenuItem.class);
        if (dto.getImage() != null) {
            entity.setImageData(Base64.getDecoder().decode(dto.getImage()));
        }
        return mapper.map(repository.save(entity), MenuItemDTO.class);
    }

    public MenuItemDTO update(Long id, MenuItemDTO dto) {
        MenuItem existing = repository.findById(id).orElseThrow();
        mapper.map(dto, existing);
        if (dto.getImage() != null) {
            existing.setImageData(Base64.getDecoder().decode(dto.getImage()));
        }
        return mapper.map(repository.save(existing), MenuItemDTO.class);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public MenuItemDTO toggleAvailability(Long id) {
        MenuItem item = repository.findById(id).orElseThrow();
        item.setAvailable(!item.isAvailable());
        return mapper.map(repository.save(item), MenuItemDTO.class);
    }
}
