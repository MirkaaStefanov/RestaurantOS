package com.example.RestaurantOS.repositories.old;

import com.example.RestaurantOS.models.entity.old.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

}
