package com.archsense.user.config;

import com.archsense.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(secret, expirationMs);
    }
}