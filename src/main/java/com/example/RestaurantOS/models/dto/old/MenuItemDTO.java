package com.example.RestaurantOS.models.dto.old;

import com.example.RestaurantOS.enums.MenuCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemDTO {

    public Long id;
    private String name;
    private String description;
    private double price;
    private MenuCategory category;
    private boolean available = true;
    private Integer preparationTime;
    private String imageFileName;
    private String image;

    @JsonIgnore
    private transient MultipartFile imageFile;


}
