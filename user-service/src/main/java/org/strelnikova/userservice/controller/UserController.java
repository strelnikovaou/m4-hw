package org.strelnikova.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.strelnikova.userservice.controller.dto.UserRequestDTO;
import org.strelnikova.userservice.controller.dto.UserResponseDTO;
import org.strelnikova.userservice.service.UserService;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "Users", description = "User management endpoints")
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users", description = "Returns a list of all users")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserResponseDTO>>> getAllUsers() {
       log.info("fetching all users");
        final List<UserResponseDTO> allUsers = userService.getAllUsers();
        List<EntityModel<UserResponseDTO>> users = allUsers.stream()
                .map(user -> EntityModel.of(user,
                        linkTo(methodOn(UserController.class).getUser(user.id())).withSelfRel(),
                        linkTo(methodOn(UserController.class).getAllUsers()).withRel("users")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UserResponseDTO>> collection = CollectionModel.of(users,
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Get user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("{id}")
    public ResponseEntity<EntityModel<UserResponseDTO>> getUser(@PathVariable("id") UUID id) {
        log.info("Fetching user with id: {}", id);

        UserResponseDTO user = userService.getUserById(id);
        EntityModel<UserResponseDTO> userModel = EntityModel.of(user,
                linkTo(methodOn(UserController.class).getUser(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));

        return ResponseEntity.ok(userModel);
    }

    @Operation(summary = "Create a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
    })
    @PostMapping
    public ResponseEntity<EntityModel<UserResponseDTO>> createUser(@RequestBody UserRequestDTO requestDTO) {
        log.info("Creating a new user: {}", requestDTO);

        UserResponseDTO responseDTO = userService.createUser(requestDTO);
        EntityModel<UserResponseDTO> userModel = EntityModel.of(responseDTO,
                linkTo(methodOn(UserController.class).getUser(responseDTO.id())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(responseDTO.id())
                .toUri();

        return ResponseEntity.created(location).body(userModel);
    }

    @Operation(summary = "Update an existing user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDTO>> updateUser(
            @PathVariable("id") UUID id,
            @RequestBody UserRequestDTO requestDTO) {
        log.info("Updating user with id {}", id);

        UserResponseDTO updatedUser = userService.updateUser(id, requestDTO);
        EntityModel<UserResponseDTO> userModel = EntityModel.of(updatedUser,
                linkTo(methodOn(UserController.class).getUser(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));

        return ResponseEntity.ok(userModel);
    }

    @Operation(summary = "Delete a user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") UUID id) {
        log.info("Deleting user with id {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}