package com.mecanico.marketplace.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI marketplaceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Mecanico Marketplace API")
                .description("CRUD API for part listings and purchase orders")
                .version("v1"));
    }
}
