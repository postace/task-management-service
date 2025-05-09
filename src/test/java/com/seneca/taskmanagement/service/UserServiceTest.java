package com.seneca.taskmanagement.service;

import com.seneca.taskmanagement.domain.User;
import com.seneca.taskmanagement.dto.UserDto;
import com.seneca.taskmanagement.dto.UserUpdateDto;
import com.seneca.taskmanagement.exception.ResourceAlreadyExistsException;
import com.seneca.taskmanagement.exception.ResourceNotFoundException;
import com.seneca.taskmanagement.mapper.UserMapper;
import com.seneca.taskmanagement.mapper.UserMapperImpl;
import com.seneca.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserMapper userMapper;
    private UserService userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
        userService = new UserService(userRepository, userMapper);

        UUID userId = UUID.randomUUID();
        userDto = UserDto.builder()
                .id(userId)
                .username("testuser")
                .fullName("Test User")
                .build();

        user = User.builder()
                .id(userId)
                .username("testuser")
                .fullName("Test User")
                .build();
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserDto result = userService.createUser(userDto);

        // Assert
        assertNotNull(result);
        assertEquals(userDto.getUsername(), result.getUsername());
        assertEquals(userDto.getFullName(), result.getFullName());

        verify(userRepository).existsByUsernameIncludingDeleted(userDto.getUsername());
        verify(userRepository).save(Mockito.any());
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsernameIncludingDeleted(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> userService.createUser(userDto));
        verify(userRepository).existsByUsernameIncludingDeleted(userDto.getUsername());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

        // Act
        UserDto result = userService.getUserById(user.getId());

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getFullName(), result.getFullName());
        verify(userRepository).findById(user.getId());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(UUID.randomUUID()));
        verify(userRepository).findById(any(UUID.class));
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserDto> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).getId());
        assertEquals(user.getUsername(), result.get(0).getUsername());
        assertEquals(user.getFullName(), result.get(0).getFullName());

        verify(userRepository).findAll();
    }

    @Test
    void updateUser_Success() {
        // Arrange
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setFullName("Updated Name");
        
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserDto result = userService.updateUser(user.getId(), updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals("Updated Name", result.getFullName());

        verify(userRepository).findById(user.getId());
        verify(userRepository).save(any());
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        // Arrange
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setFullName("Updated Name");
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(UUID.randomUUID(), updateDto));
        verify(userRepository).findById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        UUID testId = UUID.randomUUID();
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(User.builder().id(testId).build()));

        // Act
        userService.deleteUser(testId);

        // Assert
        verify(userRepository).findById(testId);
        verify(userRepository).save(Mockito.any());
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(UUID.randomUUID()));
        verify(userRepository).findById(any(UUID.class));
        verifyNoMoreInteractions(userRepository);
    }
}
