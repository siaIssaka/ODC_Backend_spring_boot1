package com.example.ODC_Academy.admin;

import com.example.ODC_Academy.user.Role;
import com.example.ODC_Academy.user.User;
import com.example.ODC_Academy.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Premier administrateur : créé au démarrage s'il n'existe aucun ADMIN et si ODC_ADMIN_EMAIL / ODC_ADMIN_PASSWORD sont définis. */
@Component
public class AdminBootstrap implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository users, PasswordEncoder encoder,
                          @Value("${ODC_ADMIN_EMAIL:}") String email, @Value("${ODC_ADMIN_PASSWORD:}") String password) {
        this.users = users; this.encoder = encoder; this.email = email; this.password = password;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.length() < 8) return;
        if (users.findAll().stream().anyMatch(u -> u.getRole() == Role.ADMIN)) return;
        users.save(User.builder().prenom("Admin").nom("ODC").email(email.trim().toLowerCase())
                .password(encoder.encode(password)).role(Role.ADMIN).build());
    }
}
