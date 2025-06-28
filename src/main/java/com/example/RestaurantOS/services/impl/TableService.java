package com.example.RestaurantOS.services.impl;

import com.example.RestaurantOS.enums.TableStatus;
import com.example.RestaurantOS.models.dto.MenuItemDTO;
import com.example.RestaurantOS.models.dto.TableDTO;
import com.example.RestaurantOS.models.entity.Order;
import com.example.RestaurantOS.models.entity.Table;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.repositories.OrderRepository;
import com.example.RestaurantOS.repositories.TableRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final ModelMapper modelMapper;
    private final UserServiceImpl userService;
    private final OrderRepository orderRepository;

    public TableDTO save(TableDTO tableDTO) {
        Table table = modelMapper.map(tableDTO, Table.class);
        return modelMapper.map(tableRepository.save(table), TableDTO.class);
    }

    public List<TableDTO> getAll() {
        List<Table> tables = tableRepository.findAll();
        return tables.stream()
                .map(table -> {
                    TableDTO dto = modelMapper.map(table, TableDTO.class);
                    return dto;
                }).collect(Collectors.toList());
    }

    public List<TableDTO> getWaitersTable() throws ChangeSetPersister.NotFoundException {
        User authenticatedUser = userService.findMe();
        List<Table> tables = tableRepository.findByWaiter(authenticatedUser);
        return tables.stream()
                .map(table -> {
                    TableDTO dto = modelMapper.map(table, TableDTO.class);
                    return dto;
                }).collect(Collectors.toList());
    }

    public TableDTO findById(UUID id) throws ChangeSetPersister.NotFoundException {
        Table table = tableRepository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        return modelMapper.map(table, TableDTO.class);
    }

    public TableDTO update(UUID id, TableDTO tableDTO) {
        Table table = modelMapper.map(tableDTO, Table.class);
        table.setId(id);
        return modelMapper.map(tableRepository.save(table), TableDTO.class);
    }

    public void delete(UUID id) throws ChangeSetPersister.NotFoundException {
        Table table = tableRepository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        tableRepository.delete(table);
    }

    public void useTable(UUID id) throws ChangeSetPersister.NotFoundException {
        Table table = tableRepository.findById(id).orElseThrow(ChangeSetPersister.NotFoundException::new);
        table.setStatus(TableStatus.OCCUPIED);
        Table savedTable = tableRepository.save(table);
        User authenticatedUser = userService.findMe();
        Order order = new Order();
        order.setUser(authenticatedUser);
        order.setTable(savedTable);
        orderRepository.save(order);
    }

}
