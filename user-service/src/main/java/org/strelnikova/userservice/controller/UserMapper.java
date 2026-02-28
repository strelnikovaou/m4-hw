package org.strelnikova.userservice.controller;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Component;
import org.strelnikova.userservice.controller.dto.UserRequestDTO;
import org.strelnikova.userservice.controller.dto.UserResponseDTO;
import org.strelnikova.userservice.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponseDTO userToResponseDTO(User user);

    User requestDTOToUser(UserRequestDTO requestDTO);

}
