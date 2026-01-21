package com.example.RestaurantOS.repositories;

import com.example.RestaurantOS.models.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import java.nio.file.LinkOption;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
