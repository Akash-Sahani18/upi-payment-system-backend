package com.upi.upi_payment_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI upiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UPI Payment System API")
                        .description("Backend APIs for UPI payment simulation")
                        .version("1.0"));
    }
}
