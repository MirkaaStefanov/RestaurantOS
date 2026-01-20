package com.example.RestaurantOS.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "memberships")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private MembershipPlan plan;

    @ManyToOne
    // Това спира рекурсията! Казваме: "Не включвай тези полета, когато сериализираш owner-а тук"
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"currentMembership", "memberships", "password", "activeDeviceId"})
    @ToString.Exclude
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "membership_users",
            joinColumns = @JoinColumn(name = "membership_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"currentMembership", "memberships", "password", "activeDeviceId"})
//    @com.fasterxml.jackson.annotation.JsonIgnore // <--- ДОБАВЕТЕ ТОВА
    private List<User> users;

    private LocalDate startDate;
    private LocalDate endDate;
    private int remainingVisits; // -1 = unlimited
    @Column(name = "is_valid")
    private boolean valid;

    @Transient
    private boolean dailyAccessActive;

    @PostLoad
    private void checkValidityAfterLoad() {
        recalculateAndSetValidity();
    }
    @PrePersist
    @PreUpdate
    private void checkValidityBeforeSave() {
        recalculateAndSetValidity();
    }

    private void recalculateAndSetValidity() {

        boolean shouldBeValid = true;
        LocalDate now = LocalDate.now();

        // 1. Проверка за крайна дата
        if (this.endDate != null && now.isAfter(this.endDate)) {
            shouldBeValid = false;
        }

        // 2. Проверка за начална дата
        if (this.startDate != null && now.isBefore(this.startDate)) {
            shouldBeValid = false;
        }

        // 3. Проверка за посещения
        if (this.remainingVisits != -1 && this.remainingVisits <= 0) {
            shouldBeValid = false;
        }

        // ВАЖНО: Променяме полето само ако има разлика.
        // Това маркира обекта като "мръсен" (modified) за Hibernate.
        if (this.valid != shouldBeValid) {
            this.valid = shouldBeValid;
        }
    }
}
