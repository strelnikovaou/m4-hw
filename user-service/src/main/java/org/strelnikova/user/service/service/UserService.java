package org.strelnikova.user.service.service;

import org.strelnikova.user.service.controller.dto.UserRequestDTO;
import org.strelnikova.user.service.controller.dto.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(UUID id);

    UserResponseDTO createUser(UserRequestDTO requestDTO);

    UserResponseDTO updateUser(UUID id, UserRequestDTO requestDTO);

    void deleteUser(UUID id);

}
