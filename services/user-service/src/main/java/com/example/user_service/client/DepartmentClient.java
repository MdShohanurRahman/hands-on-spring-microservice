package com.example.user_service.client;

import com.example.user_service.dto.DepartmentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "department-service", path = "/api/v1/departments", fallback = DepartmentClientFallback.class)
public interface DepartmentClient {

    @GetMapping("/{id}")
    DepartmentDto getDepartmentById(@PathVariable("id") Long id);
}
