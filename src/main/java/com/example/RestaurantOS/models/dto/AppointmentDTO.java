package com.example.RestaurantOS.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentDTO {

    public Long id;

    public LocalDateTime appointmentDate;

    public String clientName;

    public String clientPhone;


}
