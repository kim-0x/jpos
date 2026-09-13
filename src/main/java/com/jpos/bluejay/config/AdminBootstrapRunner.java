package com.jpos.bluejay.config;

import com.jpos.user.model.UserRole;
import com.jpos.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {
    private final UserRepository userRepository;

    @Value("${bluejay.bootstrap.admin.username:}")
    private String adminUsername;

    @Value("${bluejay.bootstrap.admin.secret:}")
    private String adminSecret;

    public AdminBootstrapRunner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminUsername == null || adminUsername.isBlank() || adminSecret == null || adminSecret.isBlank()) {
            return;
        }
        if (userRepository.isNameTaken(adminUsername)) {
            return;
        }
        try {
            userRepository.addUser(adminUsername, adminSecret, UserRole.ADMIN.getValue());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to bootstrap admin account", e);
        }
    }
}
