package com.busgo;

import com.busgo.user.RoleRepository;
import com.busgo.user.User;
import com.busgo.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
public class StartupRunner {

    @Bean
    CommandLineRunner seedRolesAndAdmin(RoleRepository roleRepo, UserRepository userRepo, PasswordEncoder passwordEncoder,
                                       @Value("${app.admin.email:}") String adminEmail,
                                       @Value("${app.admin.password:}") String adminPassword) {
        return args -> {
            // Roles seeded by Flyway V1; here ensure admin user exists if env provided
            if (adminEmail != null && !adminEmail.isBlank() && adminPassword != null && !adminPassword.isBlank()) {
                if (userRepo.findByEmail(adminEmail).isEmpty()) {
                    User admin = new User();
                    admin.setId(UUID.randomUUID());
                    admin.setFullName("Administrator");
                    admin.setEmail(adminEmail);
                    admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                    userRepo.save(admin);
                    System.out.println("Created default admin: " + adminEmail);
                }
            }
        };
    }
}
