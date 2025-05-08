package com.seneca.taskmanagement.mapper;

import com.seneca.taskmanagement.domain.User;
import com.seneca.taskmanagement.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);

    @Mapping(target = "tasks", ignore = true)
    User toEntity(UserDto userDto);

    @Mapping(target = "tasks", ignore = true)
    void updateUserFromDto(UserDto userDto, @MappingTarget User user);
}
