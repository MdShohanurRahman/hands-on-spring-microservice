package com.example.user_service.service.impl;

import com.example.user_service.dto.UserDto;
import com.example.user_service.entity.UserEntity;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserDto mapToDto(UserEntity entity) {
        if (entity == null) return null;
        return UserDto.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .departmentId(entity.getDepartmentId())
                .build();
    }

    private UserEntity mapToEntity(UserDto dto) {
        if (dto == null) return null;
        return UserEntity.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .departmentId(dto.getDepartmentId())
                .build();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        UserEntity toSave = mapToEntity(userDto);
        toSave.setId(null);
        UserEntity saved = userRepository.save(toSave);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        return mapToDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        UserEntity existing = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        existing.setFirstName(userDto.getFirstName());
        existing.setLastName(userDto.getLastName());
        existing.setEmail(userDto.getEmail());
        existing.setDepartmentId(userDto.getDepartmentId());
        UserEntity saved = userRepository.save(existing);
        return mapToDto(saved);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
