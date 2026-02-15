package org.strelnikova.m4hw.service;

import org.strelnikova.m4hw.controller.dto.UserRequestDTO;
import org.strelnikova.m4hw.controller.dto.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(UUID id);

    UserResponseDTO createUser(UserRequestDTO requestDTO);

    UserResponseDTO updateUser(UUID id, UserRequestDTO requestDTO);

    void deleteUser(UUID id);

}
