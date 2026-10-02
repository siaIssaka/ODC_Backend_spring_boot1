package com.example.ODC_Academy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {



    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "Admin123!";

        String hash = encoder.encode(password);

        System.out.println("Mot de passe : " + password);
        System.out.println("Hash BCrypt : " + hash);
    }
}
