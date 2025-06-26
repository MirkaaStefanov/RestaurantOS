package com.example.RestaurantOS.services;



import com.example.RestaurantOS.models.dto.auth.AdminUserDTO;
import com.example.RestaurantOS.models.dto.auth.OAuth2UserInfoDTO;
import com.example.RestaurantOS.models.dto.auth.PublicUserDTO;
import com.example.RestaurantOS.models.dto.auth.RegisterRequest;
import com.example.RestaurantOS.models.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User createUser(RegisterRequest request);

    User findByEmail(String email);

    List<AdminUserDTO> getAllUsers();

    AdminUserDTO getByIdAdmin(UUID id);

    AdminUserDTO updateUser(UUID id, AdminUserDTO userDTO, PublicUserDTO currentUser);

    void deleteUserById(UUID id, PublicUserDTO currentUser);

    User processOAuthUser(OAuth2UserInfoDTO oAuth2User);

    User findById(UUID id);
}
