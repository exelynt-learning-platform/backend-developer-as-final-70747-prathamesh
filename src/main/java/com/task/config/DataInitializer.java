package com.task.config;

import com.task.entity.Resource;
import com.task.entity.User;
import com.task.enums.Role;
import com.task.repository.ResourceRepository;
import com.task.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final PasswordEncoder encoder;

    public DataInitializer(UserRepository userRepository,
                           ResourceRepository resourceRepository,
                           PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        initUsers();
        initResources();
    }

    private void initUsers() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(new User(null, "admin", encoder.encode("admin123"), "admin@task.com", Role.ROLE_ADMIN));
        }

        if (!userRepository.existsByUsername("user")) {
            userRepository.save(new User(null, "user", encoder.encode("user123"), "user@task.com", Role.ROLE_USER));
        }
    }

    private void initResources() {
        if (resourceRepository.count() == 0) {
            resourceRepository.saveAll(List.of(
                    new Resource(null, "Conference Room A", "10-person room with projector and whiteboard", new BigDecimal("50.00"), true),
                    new Resource(null, "MacBook Pro 16", "High performance development laptop", new BigDecimal("15.00"), true),
                    new Resource(null, "Electric Scooter #1", "City commuter electric scooter", new BigDecimal("8.50"), true)
            ));
        }
    }
}
