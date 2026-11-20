package com.mecanico.vehiclepassport.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Prints the useful local URLs once the service is ready.
 * IntelliJ and most terminals turn them into clickable links.
 */
@Component
public class StartupLinksLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupLinksLogger.class);

    private final String port;

    public StartupLinksLogger(@Value("${server.port:8082}") String port) {
        this.port = port;
    }

    @Override
    public void run(ApplicationArguments args) {
        String base = "http://localhost:" + port;
        log.info("");
        log.info("============================================================");
        log.info(" Mecanico Vehicle Passport is ready");
        log.info("------------------------------------------------------------");
        log.info(" Health      : " + base + "/actuator/health");
        log.info(" Swagger UI  : " + base + "/swagger-ui.html");
        log.info(" OpenAPI     : " + base + "/v3/api-docs");
        log.info(" API base    : " + base + "/api");
        log.info(" Gateway API : http://localhost:8084/api");
        log.info(" Eureka      : http://localhost:8761");
        log.info(" Portal      : http://localhost:4201");
        log.info(" Users portal: http://localhost:4200");
        log.info("============================================================");
        log.info("");
    }
}
