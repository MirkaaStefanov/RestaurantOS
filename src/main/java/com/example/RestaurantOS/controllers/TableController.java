package com.example.RestaurantOS.controllers;


import com.example.RestaurantOS.models.dto.OrderDTO;
import com.example.RestaurantOS.models.dto.TableDTO;
import com.example.RestaurantOS.models.entity.Table;
import com.example.RestaurantOS.services.impl.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<List<TableDTO>> getAll(@RequestHeader(value = "Authorization", required = false) String auth) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(tableService.getAll());
    }

    @GetMapping("/waiter")
    public ResponseEntity<List<TableDTO>> findForWaiter(@RequestHeader(value = "Authorization", required = true) String auth) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(tableService.getWaitersTable());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableDTO> getById(@PathVariable UUID id, @RequestHeader(value = "Authorization", required = false) String auth) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(tableService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TableDTO> create(@RequestBody TableDTO dto, @RequestHeader(value = "Authorization", required = true) String auth) {
        return ResponseEntity.ok(tableService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TableDTO> update(@PathVariable UUID id, @RequestBody TableDTO dto, @RequestHeader(value = "Authorization", required = true) String auth) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(tableService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @RequestHeader(value = "Authorization", required = true) String auth) throws ChangeSetPersister.NotFoundException {
        tableService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/use/{id}")
    public ResponseEntity<OrderDTO> use(@PathVariable UUID id, @RequestHeader(value = "Authorization", required = false) String auth) throws ChangeSetPersister.NotFoundException {
        return ResponseEntity.ok(tableService.useTable(id));
    }

}

