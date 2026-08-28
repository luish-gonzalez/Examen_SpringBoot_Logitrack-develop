package com.logitrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_SEGURIDAD = "bearerAuth";

    @Bean
    public OpenAPI logitrackOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("LogiTrack API")
                        .description(
                                "API REST para la gestión de bodegas, "
                                        + "productos, movimientos de inventario, "
                                        + "auditorías y reportes de LogiTrack."
                        )
                        .version("1.0.0")
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(ESQUEMA_SEGURIDAD)
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        ESQUEMA_SEGURIDAD,
                                        crearEsquemaJwt()
                                )
                );
    }

    private SecurityScheme crearEsquemaJwt() {
        return new SecurityScheme()
                .name(ESQUEMA_SEGURIDAD)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description(
                        "Ingrese el token JWT obtenido en /auth/login. "
                                + "Swagger agregará automáticamente el prefijo Bearer."
                );
    }
}