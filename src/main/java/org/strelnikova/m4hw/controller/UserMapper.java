package org.strelnikova.m4hw.controller;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.strelnikova.m4hw.controller.dto.UserRequestDTO;
import org.strelnikova.m4hw.controller.dto.UserResponseDTO;
import org.strelnikova.m4hw.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponseDTO userToResponseDTO(User user);

    User requestDTOToUser(UserRequestDTO requestDTO);

}
