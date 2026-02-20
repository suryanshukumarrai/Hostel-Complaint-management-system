package com.hostel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class
ComplaintManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ComplaintManagementApplication.class, args);
    }
}
