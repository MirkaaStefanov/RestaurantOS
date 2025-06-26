package com.example.RestaurantOS.models.entity;

import com.example.RestaurantOS.enums.MenuCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    private String name;
    private String description;
    private double price;

    @Enumerated(EnumType.STRING)
    private MenuCategory category;
    private boolean available = true;
    private Integer preparationTime;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB", nullable = false)
    private byte[] imageData;

    public String getBase64Image() {
        if (this.imageData == null) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(this.imageData);
    }

}
