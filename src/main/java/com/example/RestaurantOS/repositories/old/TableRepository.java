package com.example.RestaurantOS.repositories.old;

import com.example.RestaurantOS.models.entity.old.Table;
import com.example.RestaurantOS.models.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TableRepository extends JpaRepository<Table, UUID> {
    List<Table> findByWaiter(User waiter);
}
