package org.strelnikova.m4hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.strelnikova.m4hw.controller.UserMapper;
import org.strelnikova.m4hw.controller.dto.UserRequestDTO;
import org.strelnikova.m4hw.controller.dto.UserResponseDTO;
import org.strelnikova.m4hw.exception.UserNotFoundException;
import org.strelnikova.m4hw.exception.ValidationException;
import org.strelnikova.m4hw.model.User;
import org.strelnikova.m4hw.repository.UserRepository;
import org.strelnikova.m4hw.validation.UserValidator;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Unit Tests")
public class UserServiceTest {
    private static final String TEST_NAME = "Ivan";
    private static final String TEST_EMAIL = "ivan@gmail.com";
    private static final int TEST_AGE = 25;
    private static final String NEW_NAME = "Petr";
    private static final String NEW_EMAIL = "petr@gmail.com";
    private static final int NEW_AGE = 30;
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private UserServiceImpl userService;


    @Nested
    @DisplayName("Create User")
    class CreateUserTests {
        @Test
        @DisplayName("Should create user when data is valid")
        void shouldCreateUser() {

            UserRequestDTO request = new UserRequestDTO(TEST_NAME, TEST_EMAIL, TEST_AGE);
            UUID generatedId = UUID.randomUUID();
            OffsetDateTime createdAt = OffsetDateTime.now();

            UserResponseDTO expectedResponse = new UserResponseDTO(
                    generatedId, TEST_NAME, TEST_EMAIL, TEST_AGE, createdAt
            );

            doNothing().when(userValidator).validate(request);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);

            when(userMapper.requestDTOToUser(any(UserRequestDTO.class))).thenReturn(new User(TEST_NAME, TEST_EMAIL, TEST_AGE));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(userMapper.userToResponseDTO(any(User.class))).thenReturn(expectedResponse);

            UserResponseDTO result = userService.createUser(request);

            assertThat(result.name()).isEqualTo(TEST_NAME);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getName()).isEqualTo(TEST_NAME);
            assertThat(savedUser.getEmail()).isEqualTo(TEST_EMAIL);

        }

        @Test
        @DisplayName("Should not save when validation fails")
        void shouldNotSaveWhenValidationFails() {
            UserRequestDTO request = new UserRequestDTO("", "invalid", -5);

            doThrow(new ValidationException("Invalid data"))
                    .when(userValidator).validate(request);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Invalid data");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when email already exists")
        void shouldThrowWhenEmailExists() {
            UserRequestDTO request = new UserRequestDTO(TEST_NAME, TEST_EMAIL, TEST_AGE);

            doNothing().when(userValidator).validate(request);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email already in use");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get User")
    class GetUserTests {

        @Test
        @DisplayName("Should return user by id")
        void shouldReturnUserById() {
            UUID id = UUID.randomUUID();
            OffsetDateTime createdAt = OffsetDateTime.now();

            UserResponseDTO expectedResponse = new UserResponseDTO(
                    id, TEST_NAME, TEST_EMAIL, TEST_AGE, createdAt
            );

            when(userRepository.findById(id)).thenReturn(Optional.of(new User(TEST_NAME, TEST_EMAIL, TEST_AGE)));
            when(userMapper.userToResponseDTO(any(User.class))).thenReturn(expectedResponse);

            UserResponseDTO result = userService.getUserById(id);

            assertThat(result.id()).isEqualTo(id);
            assertThat(result.name()).isEqualTo(TEST_NAME);
            verify(userRepository).findById(id);
            verifyNoInteractions(userValidator);
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get All Users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return all users")
        void shouldReturnAllUsers() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            OffsetDateTime now = OffsetDateTime.now();

            when(userRepository.findAll()).thenReturn(List.of(
                    new User(TEST_NAME, TEST_EMAIL, TEST_AGE),
                    new User(NEW_NAME, NEW_EMAIL, NEW_AGE)
            ));

            when(userMapper.userToResponseDTO(any(User.class)))
                    .thenReturn(
                            new UserResponseDTO(id1, TEST_NAME, TEST_EMAIL, TEST_AGE, now),
                            new UserResponseDTO(id2, NEW_NAME, NEW_EMAIL, NEW_AGE, now)
                    );

            List<UserResponseDTO> result = userService.getAllUsers();

            assertThat(result.size() == 2);
            assertThat(result.get(0).name()).isEqualTo(TEST_NAME);
            assertThat(result.get(1).name()).isEqualTo(NEW_NAME);
        }
    }

    @Nested
    @DisplayName("Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user")
        void shouldUpdateUser() {
            UUID id = UUID.randomUUID();
            OffsetDateTime createdAt = OffsetDateTime.now();

            UserRequestDTO request = new UserRequestDTO(NEW_NAME, NEW_EMAIL, NEW_AGE);
            UserResponseDTO expectedResponse = new UserResponseDTO(id, NEW_NAME, NEW_EMAIL, NEW_AGE, createdAt);

            User existingUser = new User(TEST_NAME, TEST_EMAIL, TEST_AGE);

            doNothing().when(userValidator).validate(request);
            when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(new User(NEW_NAME, NEW_EMAIL, NEW_AGE));
            when(userMapper.userToResponseDTO(any(User.class))).thenReturn(expectedResponse);

            UserResponseDTO result = userService.updateUser(id, request);

            assertThat(result.name()).isEqualTo(NEW_NAME);
            verify(userValidator).validate(request);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw when validation fails on update")
        void shouldThrowWhenValidationFailsOnUpdate() {
            UUID id = UUID.randomUUID();
            UserRequestDTO request = new UserRequestDTO("", "invalid", -5);

            doThrow(new ValidationException("Name cannot be empty"))
                    .when(userValidator).validate(request);

            assertThatThrownBy(() -> userService.updateUser(id, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Name cannot be empty");

            verify(userRepository, never()).findById(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when updating to existing email")
        void shouldThrowWhenUpdatingToExistingEmail() {
            UUID id = UUID.randomUUID();
            UserRequestDTO request = new UserRequestDTO(TEST_NAME, NEW_EMAIL, TEST_AGE);
            User existingUser = new User(TEST_NAME, TEST_EMAIL, TEST_AGE);

            doNothing().when(userValidator).validate(request);
            when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUser(id, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email already in use");
        }
    }

    @Nested
    @DisplayName("Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete existing user")
        void shouldDeleteUser() {
            UUID id = UUID.randomUUID();

            when(userRepository.existsById(id)).thenReturn(true);
            doNothing().when(userRepository).deleteById(id);

            assertThatCode(() -> userService.deleteUser(id)).doesNotThrowAnyException();

            verify(userRepository).deleteById(id);
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();

            when(userRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

}
