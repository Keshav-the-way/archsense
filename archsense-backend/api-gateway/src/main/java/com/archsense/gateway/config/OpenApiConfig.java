package com.archsense.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI archsenseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ArchSense API")
                        .description("AI-Powered Distributed System Design Analysis Platform\n\n" +
                                "## Features\n" +
                                "- User registration and authentication (JWT)\n" +
                                "- Project management\n" +
                                "- Artifact upload (diagrams, documents)\n" +
                                "- AI-powered architecture analysis\n" +
                                "- Detailed analysis reports with issues and recommendations\n\n" +
                                "## Authentication\n" +
                                "1. Register or login to get a JWT token\n" +
                                "2. Click 'Authorize' button below\n" +
                                "3. Enter: `Bearer <your-token>`\n" +
                                "4. All subsequent requests will include the token")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("ArchSense Team")
                                .email("support@archsense.com")
                                .url("https://github.com/archsense"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from /api/users/login or /api/users/register")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}