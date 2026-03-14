package org.strelnikova.user.service.controller;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.strelnikova.user.service.controller.dto.UserRequestDTO;
import org.strelnikova.user.service.controller.dto.UserResponseDTO;
import org.strelnikova.user.service.model.User;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponseDTO userToResponseDTO(User user);

    User requestDTOToUser(UserRequestDTO requestDTO);

    List<UserResponseDTO> usersToResponseDTOs(List<User> users);

}
