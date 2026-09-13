package com.jpos.bluejay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.jpos", "utils"})
public class BlueJayApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlueJayApplication.class, args);
    }
}
