package com.inspectron.inspectron.infra.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Inspectron API",
                version = "v1",
                description = "API de inspeção de placas para o time Inspectron",
                contact = @Contact(name = "Inspectron", email = "support@inspectron.com"),
                license = @io.swagger.v3.oas.annotations.info.License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html")),
        security = @SecurityRequirement(name = "bearer-key"))
@SecurityScheme(name = "bearer-key", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("Inspectron")
                .pathsToMatch("/auth/**", "/users/**", "/health/**")
                .build();
    }

    @Bean
    public OpenAPI inspectronOpenApi() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Inspectron API")
                        .version("v1")
                        .description("API de inspeção de placas para o time Inspectron"))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearer-key"));
    }
}
