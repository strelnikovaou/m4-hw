package org.strelnikova.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.strelnikova.userservice.controller.UserMapper;
import org.strelnikova.userservice.controller.dto.UserRequestDTO;
import org.strelnikova.userservice.controller.dto.UserResponseDTO;
import org.strelnikova.userservice.exception.UserNotFoundException;
import org.strelnikova.userservice.model.User;
import org.strelnikova.userservice.model.UserEventType;
import org.strelnikova.userservice.model.outbox.OutboxEvent;
import org.strelnikova.userservice.model.outbox.dto.UserEventPayload;
import org.strelnikova.userservice.repository.OutboxEventRepository;
import org.strelnikova.userservice.repository.UserRepository;
import org.strelnikova.userservice.validation.UserValidator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

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

        saveOutboxEvent(savedUser, UserEventType.CREATED);
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
        saveOutboxEvent(updatedUser, UserEventType.UPDATED);
        return userMapper.userToResponseDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.deleteById(id);
        saveOutboxEvent(user, UserEventType.DELETED);
        log.info("User deleted: {}", id);
    }

    private void saveOutboxEvent(User user, UserEventType eventType) {
        try {
            UserEventPayload payload = new UserEventPayload(user.getName(), user.getEmail());
            String payloadJson = objectMapper.writeValueAsString(payload);  // строка
            OutboxEvent event = new OutboxEvent(user.getId(), eventType.name(), payloadJson);
            outboxEventRepository.save(event);
            log.debug("Outbox event saved for user {} with type {}", user.getId(), eventType);
        } catch (Exception e) {
            log.error("Failed to save outbox event for user {}", user.getId(), e);
        }
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
