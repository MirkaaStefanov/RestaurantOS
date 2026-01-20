package com.example.RestaurantOS.models.entity;

import com.example.RestaurantOS.enums.PlanType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@jakarta.persistence.Table(name = "membership_plans")
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;          // "Monthly", "Family 4"
    private int durationDays;      // 30, 90, 365
    private int visitLimit;        // -1 = unlimited, 1 = single, >1 = family
    private BigDecimal price;
    private boolean family;
    private int maxInFamily;

}
