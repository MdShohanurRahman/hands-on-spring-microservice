package com.example.user_service.service;

import com.example.user_service.dto.UserDto;
import com.example.user_service.dto.UserWithDepartmentDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUserById(Long id);
    List<UserDto> getAllUsers();
    UserDto updateUser(Long id, UserDto userDto);
    void deleteUser(Long id);
    UserWithDepartmentDto getUserWithDepartment(Long userId);
}
