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
import org.strelnikova.m4hw.validation.UserValidator;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;

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
        userValidator.validate(requestDTO);

        checkEmailUnique(requestDTO.email(), null);

        User user = userMapper.requestDTOToUser(requestDTO);
        User savedUser = userRepository.save(user);
        log.info("User created with id: {}", savedUser.getId());
        return userMapper.userToResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(UUID id, UserRequestDTO requestDTO) {
        log.info("Updating user with id: {}", id);

        userValidator.validate(requestDTO);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        checkEmailUnique(requestDTO.email(), existingUser.getEmail());

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

    private void checkEmailUnique(String newEmail, String currentEmail) {
        if (currentEmail != null && currentEmail.equals(newEmail)) {
            return;
        }
        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already in use: " + newEmail);
        }
    }
}
