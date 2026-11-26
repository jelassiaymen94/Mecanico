package com.mecanico.eureka.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Prints the registry dashboard URL once Eureka is ready.
 */
@Component
public class StartupLinksLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupLinksLogger.class);

    private final String port;

    public StartupLinksLogger(@Value("${server.port:8761}") String port) {
        this.port = port;
    }

    @Override
    public void run(ApplicationArguments args) {
        String base = "http://localhost:" + port;
        log.info("");
        log.info("============================================================");
        log.info(" Mecanico Eureka registry is ready");
        log.info("------------------------------------------------------------");
        log.info(" Dashboard   : " + base);
        log.info(" Health      : " + base + "/actuator/health");
        log.info(" Gateway     : http://localhost:8084");
        log.info(" Users       : http://localhost:8083");
        log.info(" Passport    : http://localhost:8082");
        log.info("============================================================");
        log.info("");
    }
}
