package org.strelnikova.m4hw.controller;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.strelnikova.m4hw.controller.dto.UserRequestDTO;
import org.strelnikova.m4hw.controller.dto.UserResponseDTO;
import org.strelnikova.m4hw.exception.UserNotFoundException;
import org.strelnikova.m4hw.service.UserService;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("User Controller Unit Tests")
class UserControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("GET /api/users - should return list of users")
    void getAllUsers() throws Exception {

        UserResponseDTO user1 = new UserResponseDTO(
                UUID.randomUUID(), "John", "john@example.com", 30, OffsetDateTime.now());
        UserResponseDTO user2 = new UserResponseDTO(
                UUID.randomUUID(), "Jane", "jane@example.com", 25, OffsetDateTime.now());

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("John")))
                .andExpect(jsonPath("$[1].name", is("Jane")));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/users/{id} - should return user")
    void getUserById() throws Exception {

        UUID id = UUID.randomUUID();
        UserResponseDTO user = new UserResponseDTO(
                id, "John", "john@example.com", 30, OffsetDateTime.now());

        when(userService.getUserById(id)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.name", is("John")));

        verify(userService, times(1)).getUserById(id);
    }

    @Test
    @DisplayName("GET /api/users/{id} - should return 404 when service throws exception")
    void getUserById_NotFound() throws Exception {

        UUID id = UUID.randomUUID();
        when(userService.getUserById(id)).thenThrow(new UserNotFoundException(id));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(id);
    }

    @Test
    @DisplayName("POST /api/users - should create user and return 201")
    void createUser() throws Exception {

        UserRequestDTO request = new UserRequestDTO("New User", "new@example.com", 28);
        UUID generatedId = UUID.randomUUID();
        UserResponseDTO response = new UserResponseDTO(
                generatedId, "New User", "new@example.com", 28, OffsetDateTime.now());

        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/users/" + generatedId)))
                .andExpect(jsonPath("$.id", is(generatedId.toString())))
                .andExpect(jsonPath("$.name", is("New User")));

        verify(userService, times(1)).createUser(any(UserRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - should update user")
    void updateUser() throws Exception {

        UUID id = UUID.randomUUID();
        UserRequestDTO request = new UserRequestDTO("Updated", "updated@example.com", 30);
        UserResponseDTO response = new UserResponseDTO(
                id, "Updated", "updated@example.com", 30, OffsetDateTime.now());

        when(userService.updateUser(eq(id), any(UserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated")));

        verify(userService, times(1)).updateUser(eq(id), any(UserRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - should return 204")
    void deleteUser() throws Exception {

        UUID id = UUID.randomUUID();
        doNothing().when(userService).deleteUser(id);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(id);
    }
}