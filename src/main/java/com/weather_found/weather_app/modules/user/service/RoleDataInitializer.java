package com.weather_found.weather_app.modules.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.weather_found.weather_app.modules.user.model.Role;
import com.weather_found.weather_app.modules.user.repository.RoleRepository;

/**
 * Initializes default roles in the database
 */
@Component
@ConditionalOnProperty(name = "app.roles.initialize", havingValue = "true", matchIfMissing = true)
public class RoleDataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRole("USER", "Basic user role for weather app access");
        initializeRole("ADMIN", "Administrator role with full system access");
    }

    private void initializeRole(String roleName, String description) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            role.setDescription(description);
            roleRepository.save(role);
        }
    }
}
