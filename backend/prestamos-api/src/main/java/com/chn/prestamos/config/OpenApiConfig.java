package com.chn.prestamos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI prestamosOpenApi() {
        return new OpenAPI().info(new Info()
            .title("API Sistema de Préstamos")
            .description(
                "Gestión de clientes, solicitudes, préstamos y pagos."
            )
            .version("1.0.0"));
    }
}
