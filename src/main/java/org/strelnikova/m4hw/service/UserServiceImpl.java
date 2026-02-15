package org.strelnikova.m4hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.strelnikova.m4hw.controller.UserMapper;
import org.strelnikova.m4hw.controller.dto.UserRequestDTO;
import org.strelnikova.m4hw.controller.dto.UserResponseDTO;
import org.strelnikova.m4hw.exception.UserNotFoundException;
import org.strelnikova.m4hw.model.User;
import org.strelnikova.m4hw.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::userToResponseDTO)
                .toList();
    }

    @Override
    public UserResponseDTO getUserById(UUID id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.userToResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        log.info("Creating new user: {}", requestDTO);
        User user = userMapper.requestDTOToUser(requestDTO);
        User savedUser = userRepository.save(user);
        log.info("User created with id: {}", savedUser.getId());
        return userMapper.userToResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(UUID id, UserRequestDTO requestDTO) {
        log.info("Updating user with id: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        existingUser.setName(requestDTO.name());
        existingUser.setEmail(requestDTO.email());
        existingUser.setAge(requestDTO.age());

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated: {}", updatedUser.getId());
        return userMapper.userToResponseDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }
}
