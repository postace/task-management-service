package com.seneca.taskmanagement.mapper;

import com.seneca.taskmanagement.domain.User;
import com.seneca.taskmanagement.dto.UserDto;
import com.seneca.taskmanagement.dto.UserUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);

    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserDto userDto);

    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateUserFromDto(UserDto userDto, @MappingTarget User user);

    /**
     * Update a user entity with non-null fields from UserUpdateDto
     * Only updates fields that are present in the DTO
     */
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateUserFromDto(UserUpdateDto updateDto, @MappingTarget User user);
}
