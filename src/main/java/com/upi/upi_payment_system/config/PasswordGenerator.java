package com.upi.upi_payment_system.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordGenerator {

    @Bean
    CommandLineRunner generateUpiPinHash() {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String hash = encoder.encode("7890");
            System.out.println("BCrypt hash for UPI PIN 7890:");
            System.out.println(hash);
        };
    }
}
