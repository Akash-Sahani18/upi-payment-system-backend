package com.upi.upi_payment_system.controller;

import com.upi.upi_payment_system.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String upiId) {
        String token = jwtUtil.generateToken(upiId);
        return Map.of("token", token);
    }
}
