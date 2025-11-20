package org.igdevx.spring_boot_account_microservice.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Account Service API")
                        .version("1.0.0")
                        .description("API documentation for the Account Service - manages user accounts, restaurant profiles, and producer profiles")
                        .contact(new Contact()
                                .name("Account Service Team")
                                .email("support@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:5001").description("Local Development"),
                        new Server().url("http://localhost:8080").description("API Gateway")
                ));
    }
}