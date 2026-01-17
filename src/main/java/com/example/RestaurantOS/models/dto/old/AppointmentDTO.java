package com.example.RestaurantOS.models.dto.old;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentDTO {

    public Long id;

    public LocalDateTime appointmentDate;

    public String clientName;

    public String clientPhone;


}
