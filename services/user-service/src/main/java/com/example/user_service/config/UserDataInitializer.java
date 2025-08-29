package com.example.user_service.config;

import com.example.user_service.entity.UserEntity;
import com.example.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class UserDataInitializer {

    @Bean
    public ApplicationRunner initUserData(UserRepository userRepository) {
        return args -> {
            long count = userRepository.count();
            if (count > 0) {
                log.info("UserDataInitializer: skipping seeding, users already present: {}", count);
                return;
            }

            log.info("UserDataInitializer: seeding dummy users...");
            userRepository.save(UserEntity.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .departmentId(1L)
                    .build());

            userRepository.save(UserEntity.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .departmentId(2L)
                    .build());

            userRepository.save(UserEntity.builder()
                    .firstName("Alice")
                    .lastName("Johnson")
                    .email("alice.johnson@example.com")
                    .departmentId(1L)
                    .build());

            log.info("UserDataInitializer: seeding completed. Total users: {}", userRepository.count());
        };
    }
}
