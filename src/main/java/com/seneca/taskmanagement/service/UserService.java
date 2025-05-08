package com.seneca.taskmanagement.service;

import com.seneca.taskmanagement.domain.User;
import com.seneca.taskmanagement.dto.UserDto;
import com.seneca.taskmanagement.exception.ResourceAlreadyExistsException;
import com.seneca.taskmanagement.exception.ResourceNotFoundException;
import com.seneca.taskmanagement.mapper.UserMapper;
import com.seneca.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Create a new user
     *
     * @param userDto user data to create
     * @return created user
     * @throws ResourceAlreadyExistsException if username already exists
     */
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new ResourceAlreadyExistsException("User with username " + userDto.getUsername() + " already exists");
        }

        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        log.info("Created user with ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    /**
     * Get a user by ID
     *
     * @param id user ID
     * @return user data
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toDto(user);
    }

    /**
     * Get all users
     *
     * @return list of all users
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toDtoList(users);
    }

    /**
     * Update a user
     *
     * @param id user ID
     * @param userDto updated user data
     * @return updated user
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        userMapper.updateUserFromDto(userDto, user);
        User updatedUser = userRepository.save(user);
        log.info("Updated user with ID: {}", updatedUser.getId());
        return userMapper.toDto(updatedUser);
    }

    /**
     * Delete a user
     *
     * @param id user ID
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }

        userRepository.deleteById(id);
        log.info("Deleted user with ID: {}", id);
    }
}
