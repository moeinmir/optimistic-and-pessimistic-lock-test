package com.meb.account_management.security

import lombok.AllArgsConstructor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.SecurityContextConfigurer
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.LogoutFilter

class HTTPRequestConfigurationAndPermissionHandler {

    @Configuration
    @EnableWebSecurity
    @AllArgsConstructor
    open class SecurityConfiguration(
        val filterChainExceptionHandler: FilterChainExceptionHandler,
        val overriddenUserDetailsSecurityService: OverriddenUserDetailsSecurityService,
        val jwtAuthenticationFilter: JwtAuthenticationFilter
    ) {
        @Bean
        open fun filterChain(http: HttpSecurity): SecurityFilterChain {
            http
                .addFilterBefore(filterChainExceptionHandler, LogoutFilter::class.java)
                .csrf { obj: CsrfConfigurer<HttpSecurity> ->
                    obj.disable()
                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
                }
                .headers { headers ->
                    headers.frameOptions { frameOptions ->
                        frameOptions.disable()
                    }
                }
                .securityContext { securityContext: SecurityContextConfigurer<HttpSecurity?> ->
                    securityContext.requireExplicitSave(
                        true
                    )
                }
                .authorizeHttpRequests { authz ->
                    authz
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers(
                            "/auth/**"
                        ).permitAll()
                        .requestMatchers("/account/**").authenticated()
                        .requestMatchers("/transaction/**").hasAuthority("ADMIN")
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                }
            return http.build()
        }

        @Bean
        open fun userDetailsService(): UserDetailsService {
            return overriddenUserDetailsSecurityService
        }

        @Bean
        open fun passwordEncoder(): PasswordEncoder {
            return BCryptPasswordEncoder()
        }
    }
}