package com.trip4hanoi.app.config;

import com.trip4hanoi.app.dto.res.APIResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile({"dev"})
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi(@Value("${openapi.service.api-docs}") String apiDocs) {
        return GroupedOpenApi.builder()
                .group(apiDocs)
                .packagesToScan("com.trip4hanoi.app")
                .addOpenApiCustomizer(globalHeaderCustomiser())
                .build();
    }

    @Bean
    public OpenApiCustomizer globalHeaderCustomiser() {
        return openApi -> openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
            ApiResponses responses = operation.getResponses();

            // Link tới Schema APIResponse tự động
            Schema<?> apiResponseSchema = new Schema<>().$ref("#/components/schemas/APIResponse");
            Content errorContent = new Content().addMediaType("application/json",
                    new MediaType().schema(apiResponseSchema));

            // Định nghĩa các mã lỗi mà GlobalExceptionHandler sẽ trả về
            addResponseIfNotExists(responses, "400", "Bad Request", errorContent);
            addResponseIfNotExists(responses, "401", "Unauthorized", errorContent);
            addResponseIfNotExists(responses, "403", "Forbidden", errorContent);
            addResponseIfNotExists(responses, "405", "Method Not Allowed", errorContent);
            addResponseIfNotExists(responses, "500", "Internal Server Error", errorContent);
        }));
    }

    private void addResponseIfNotExists(ApiResponses responses, String code, String description, Content content) {
        if (!responses.containsKey(code)) {
            responses.addApiResponse(code, new ApiResponse().description(description).content(content));
        }
    }

    @Bean
    public OpenAPI openAPI(
            @Value("${openapi.service.title}") String title,
            @Value("${openapi.service.version}") String version,
            @Value("${openapi.service.server}") String serverUrl) {

        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .servers(List.of(new Server().url(serverUrl)))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addSchemas("APIResponse", new Schema<APIResponse<?>>()
                                .type("object")
                                .addProperty("status", new Schema<>().type("integer"))
                                .addProperty("code", new Schema<>().type("integer"))
                                .addProperty("message", new Schema<>().type("string"))
                                .addProperty("data", new Schema<>().type("object"))))
                .security(List.of(new SecurityRequirement().addList(securitySchemeName)))
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description("Backend API FOR TRIP4-HANOI")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}