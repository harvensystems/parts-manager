package app.harven.partsmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI partsManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Parts Manager API")
                        .description("REST API for AI-powered electronics workshop component and inventory management system.")
                        .version("v1.0")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }

    @Bean
    public boolean enabledAI(@Value("${spring.ai.google.genai.api-key:}") String aiApiKey) {
        return aiApiKey != null && !aiApiKey.contains("mock") && !aiApiKey.trim().isEmpty();
    }
}
