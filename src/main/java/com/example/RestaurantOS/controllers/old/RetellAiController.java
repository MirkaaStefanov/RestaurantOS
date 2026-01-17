package com.example.RestaurantOS.controllers.old;

import com.example.RestaurantOS.models.dto.old.AppointmentDTO;
import com.example.RestaurantOS.models.entity.old.Appointment;
import com.example.RestaurantOS.repositories.old.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/retellAi")
@RequiredArgsConstructor
public class RetellAiController {

    private final AppointmentRepository appointmentRepository;
    private final ModelMapper modelMapper;
    
    @GetMapping("/date")
    public ResponseEntity<LocalDateTime> returnDateNow(){
        return ResponseEntity.ok(LocalDateTime.now());
    }

    @PostMapping("/create-appointment")
    public ResponseEntity<Void> createAppointment(@RequestBody AppointmentDTO appointmentDTO){
        appointmentRepository.save(modelMapper.map(appointmentDTO, Appointment.class));
        return ResponseEntity.noContent().build();
    }


}
