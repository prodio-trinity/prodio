package com.prodio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProdioApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProdioApplication.class, args);
    }
}
