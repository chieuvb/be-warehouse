package com.example.warehouse.configuration;

import com.example.warehouse.entity.Role;
import com.example.warehouse.enums.RoleEnum;
import com.example.warehouse.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * Seeds the database with initial data on application startup.
 * This runner is responsible for populating the 'roles' table
 * from the RoleEnum if it is currently empty.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("DataSeeder running...");
        seedRoles();
        log.info("DataSeeder finished.");
    }

    private void seedRoles() {
        // Check if roles already exist to make the seeding process idempotent
        if (roleRepository.count() > 0) {
            log.info("Roles table is not empty. Skipping role seeding.");
            return;
        }

        log.info("Seeding initial roles into the database.");
        Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {
            Role role = new Role();
            // The name in the database should match the enum's constant name
            // for compatibility with Spring Security's hasRole() checks.
            role.setName(roleEnum.name());
            roleRepository.save(role);
        });
        log.info("Successfully seeded {} roles.", roleRepository.count());
    }
}
