package com.example.user_service.client;

import com.example.user_service.dto.DepartmentDto;
import org.springframework.stereotype.Component;

@Component
public class DepartmentClientFallback implements DepartmentClient {

    @Override
    public DepartmentDto getDepartmentById(Long id) {
        return new DepartmentDto(id, "N/A", "N/A");
    }
}
