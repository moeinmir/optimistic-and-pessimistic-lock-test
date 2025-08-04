package com.meb.account_management.configuration



import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
open class SwaggerConfiguration {
    @Bean
    open fun customOpenAPI(): OpenAPI {
        val securityScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .name("bearer")
            .scheme("bearer")
            .bearerFormat("opaque")
            .`in`(SecurityScheme.In.HEADER)
            .name("Authorization")
        val securityComponent = Components()
            .addSecuritySchemes("bearer", securityScheme)
        val securityItem = SecurityRequirement()
            .addList("bearer")
        return OpenAPI()
            .components(securityComponent)
            .addSecurityItem(securityItem)
    }
}