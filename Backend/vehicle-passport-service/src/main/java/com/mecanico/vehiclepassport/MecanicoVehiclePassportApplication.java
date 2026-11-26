package com.mecanico.vehiclepassport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableScheduling
@EnableDiscoveryClient
public class MecanicoVehiclePassportApplication {

    public static void main(String[] args) {
        SpringApplication.run(MecanicoVehiclePassportApplication.class, args);
    }
}
