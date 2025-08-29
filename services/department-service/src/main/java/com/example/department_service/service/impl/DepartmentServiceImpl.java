package com.example.department_service.service.impl;

import com.example.department_service.dto.DepartmentDto;
import com.example.department_service.entity.DepartmentEntity;
import com.example.department_service.repository.DepartmentRepository;
import com.example.department_service.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    private DepartmentDto mapToDto(DepartmentEntity entity) {
        if (entity == null) return null;
        return DepartmentDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .build();
    }

    private DepartmentEntity mapToEntity(DepartmentDto dto) {
        if (dto == null) return null;
        return DepartmentEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .code(dto.getCode())
                .build();
    }

    @Override
    public DepartmentDto createDepartment(DepartmentDto dto) {
        DepartmentEntity toSave = mapToEntity(dto);
        toSave.setId(null);
        DepartmentEntity saved = departmentRepository.save(toSave);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long id) {
        DepartmentEntity entity = departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Department not found with id: " + id));
        return mapToDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentDto updateDepartment(Long id, DepartmentDto dto) {
        DepartmentEntity existing = departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Department not found with id: " + id));
        existing.setName(dto.getName());
        existing.setCode(dto.getCode());
        DepartmentEntity saved = departmentRepository.save(existing);
        return mapToDto(saved);
    }

    @Override
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new NoSuchElementException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }
}
