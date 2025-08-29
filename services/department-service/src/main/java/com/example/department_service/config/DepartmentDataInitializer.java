package com.example.department_service.config;

import com.example.department_service.entity.DepartmentEntity;
import com.example.department_service.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DepartmentDataInitializer {

    @Bean
    public ApplicationRunner initDepartmentData(DepartmentRepository departmentRepository) {
        return args -> {
            long count = departmentRepository.count();
            if (count > 0) {
                log.info("DepartmentDataInitializer: skipping seeding, departments already present: {}", count);
                return;
            }

            log.info("DepartmentDataInitializer: seeding dummy departments...");

            departmentRepository.save(DepartmentEntity.builder()
                    .name("Information Technology")
                    .code("IT")
                    .build());

            departmentRepository.save(DepartmentEntity.builder()
                    .name("Human Resources")
                    .code("HR")
                    .build());

            departmentRepository.save(DepartmentEntity.builder()
                    .name("Finance")
                    .code("FIN")
                    .build());

            log.info("DepartmentDataInitializer: seeding completed. Total departments: {}", departmentRepository.count());
        };
    }
}
