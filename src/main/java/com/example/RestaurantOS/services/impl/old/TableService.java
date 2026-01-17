package com.example.RestaurantOS.services.impl.old;

import com.example.RestaurantOS.enums.Role;
import com.example.RestaurantOS.enums.TableStatus;
import com.example.RestaurantOS.models.dto.old.OrderDTO;
import com.example.RestaurantOS.models.dto.old.TableDTO;
import com.example.RestaurantOS.models.entity.old.Order;
import com.example.RestaurantOS.models.entity.old.Table;
import com.example.RestaurantOS.models.entity.User;
import com.example.RestaurantOS.repositories.old.OrderRepository;
import com.example.RestaurantOS.repositories.old.TableRepository;
import com.example.RestaurantOS.repositories.UserRepository;
import com.example.RestaurantOS.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final ModelMapper modelMapper;
    private final UserServiceImpl userService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User authenticatedUser = userRepository.findByEmail(email).orElseThrow(ChangeSetPersister.NotFoundException::new);

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

    @Transactional
    public OrderDTO useTable(UUID tableId) throws ChangeSetPersister.NotFoundException {
        Table table = tableRepository.findById(tableId).orElseThrow(ChangeSetPersister.NotFoundException::new);

        if (table.getStatus() == TableStatus.OCCUPIED && table.getOrder() != null) {
            System.out.println("Table " + table.getNumber() + " is already occupied with Order ID: " + table.getOrder().getId() + ". Returning existing order DTO.");
            return modelMapper.map(table.getOrder(), OrderDTO.class);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User authenticatedUser = userRepository.findByEmail(email).orElseThrow(ChangeSetPersister.NotFoundException::new);

        if (!authenticatedUser.getRole().equals(Role.WAITER)) {
            throw new RuntimeException("The user is not waiter");
        }

        Order newOrder = new Order();
        newOrder.setUser(authenticatedUser);
        newOrder.setTable(table);
        newOrder.setOrderTime(LocalDateTime.now());
        newOrder.setTotalAmount(0.0);

        Order savedOrder = orderRepository.save(newOrder);

        table.setOrder(savedOrder);
        table.setWaiter(authenticatedUser);
        table.setStatus(TableStatus.OCCUPIED);

        tableRepository.save(table);

        return modelMapper.map(savedOrder, OrderDTO.class);
    }

    public OrderDTO getOrderForTable(UUID tableId) throws ChangeSetPersister.NotFoundException {
        Table table = tableRepository.findById(tableId).orElseThrow(ChangeSetPersister.NotFoundException::new);
        return modelMapper.map(table.getOrder(), OrderDTO.class);
    }
}


