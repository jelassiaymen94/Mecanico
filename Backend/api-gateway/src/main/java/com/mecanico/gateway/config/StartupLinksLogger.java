package com.mecanico.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Prints the useful local URLs once the gateway is ready.
 */
@Component
public class StartupLinksLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupLinksLogger.class);

    private final String port;

    public StartupLinksLogger(@Value("${server.port:8084}") String port) {
        this.port = port;
    }

    @Override
    public void run(ApplicationArguments args) {
        String base = "http://localhost:" + port;
        log.info("");
        log.info("============================================================");
        log.info(" Mecanico API Gateway is ready");
        log.info("------------------------------------------------------------");
        log.info(" Gateway     : " + base);
        log.info(" API base    : " + base + "/api");
        log.info(" Eureka      : http://localhost:8761");
        log.info(" Users API   : " + base + "/api/auth/login");
        log.info(" Passport API: " + base + "/api/vehicles");
        log.info(" Portal      : http://localhost:4201");
        log.info(" Users portal: http://localhost:4200");
        log.info("============================================================");
        log.info("");
    }
}
