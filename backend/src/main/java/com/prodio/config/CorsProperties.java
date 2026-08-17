package com.prodio.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prodio.cors")
public record CorsProperties(List<String> allowedOrigins) {
    public String[] allowedOriginsArray() {
        return allowedOrigins.toArray(String[]::new);
    }
}
