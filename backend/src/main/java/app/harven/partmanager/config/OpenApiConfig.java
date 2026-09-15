package app.harven.partmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI partManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Part Manager API")
                        .description("REST API for AI-powered electronics workshop component and inventory management system.")
                        .version("v1.0")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
