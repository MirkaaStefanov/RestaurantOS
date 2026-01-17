package com.example.RestaurantOS.models.entity.old;

import com.example.RestaurantOS.models.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    private double totalAmount;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private LocalDateTime orderTime;
    private LocalDateTime estimatedReadyTime;
    @OneToOne
    @JoinColumn(name = "table_id")
    private com.example.RestaurantOS.models.entity.old.Table table;

}
