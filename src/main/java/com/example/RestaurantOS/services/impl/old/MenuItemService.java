package com.example.RestaurantOS.services.impl.old;

import com.example.RestaurantOS.enums.MenuCategory;
import com.example.RestaurantOS.models.dto.old.MenuItemDTO;
import com.example.RestaurantOS.models.entity.old.MenuItem;
import com.example.RestaurantOS.repositories.old.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository repository;
    private final ModelMapper mapper;
    private final Path uploadPath = Paths.get("src/main/resources/static/images");

    public List<MenuItemDTO> findAll(Boolean available, MenuCategory category) {
        List<MenuItem> items = repository.findAll();

        return items.stream()
                .filter(item -> (available == null || item.isAvailable() == available) &&
                        (category == null || item.getCategory() == category))
                .map(item -> {
                    MenuItemDTO dto = mapper.map(item, MenuItemDTO.class);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public MenuItemDTO findById(Long id) {
        MenuItem item = repository.findById(id).orElseThrow();
        MenuItemDTO dto = mapper.map(item, MenuItemDTO.class);
        return dto;
    }

    public MenuItemDTO save(MenuItemDTO dto) {
        MenuItem entity = mapper.map(dto, MenuItem.class);

        // Get the Base64 string from the DTO
        String base64Image = dto.getImage();

        // Check if there is an image string to save
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                // 1. Decode the Base64 string back into bytes
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                // 2. Generate unique filename and force .jpg extension
                String uniqueFilename = UUID.randomUUID().toString() + ".jpg";

                // 3. Ensure the upload directory exists
                if (!Files.exists(this.uploadPath)) {
                    Files.createDirectories(this.uploadPath);
                }

                // 4. Define the absolute target path
                Path targetLocation = this.uploadPath.resolve(uniqueFilename);

                // 5. Write the decoded bytes to the file
                Files.write(targetLocation, imageBytes);

                // 6. Save the new unique filename to the entity
                entity.setImageFileName(uniqueFilename);

            } catch (IOException | IllegalArgumentException ex) {
                // Catches I/O errors and invalid Base64 string errors
                throw new RuntimeException("Could not decode or save Base64 image.", ex);
            }
        }

        // 7. Save entity to DB
        MenuItem savedEntity = repository.save(entity);
        MenuItemDTO savedDto = mapper.map(savedEntity, MenuItemDTO.class);

//        // 8. Set the accessible URL (optional, but good practice)
//        if (savedEntity.getImageFileName() != null) {
//            savedDto.setImageUrl("/images/" + savedEntity.getImageFileName());
//        }

        return savedDto;
    }

    public MenuItemDTO update(Long id, MenuItemDTO dto) {
        MenuItem existing = repository.findById(id).orElseThrow();
        mapper.map(dto, existing);
//        if (dto.getImage() != null) {
//            existing.setImageData(Base64.getDecoder().decode(dto.getImage()));
//        }
        return mapper.map(repository.save(existing), MenuItemDTO.class);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public MenuItemDTO toggleAvailability(Long id) throws ChangeSetPersister.NotFoundException {
        MenuItem item = repository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        item.setAvailable(!item.isAvailable());
        return mapper.map(repository.save(item), MenuItemDTO.class);
    }
}
