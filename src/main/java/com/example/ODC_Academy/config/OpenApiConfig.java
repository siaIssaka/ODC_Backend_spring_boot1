package com.example.ODC_Academy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation interactive de l'API, générée automatiquement et consultable
 * sur /swagger-ui.html. Déclare le schéma d'authentification Bearer JWT afin
 * de pouvoir tester les endpoints protégés directement depuis Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI odcAcademyOpenApi() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("EDUFLEX API — ODC Academy")
                        .description("API REST de la plateforme e-learning de l'Orange Digital Center Burkina Faso")
                        .version("v1.0.0")
                        .contact(new Contact().name("Orange Digital Center Burkina Faso").email("contact@odc-burkina.bf"))
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
