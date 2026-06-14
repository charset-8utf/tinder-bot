package com.tinderbot.telegram.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ApiSecurityProperties.class)
public class ApiSecurityConfig {

    @Bean
    @Order(0)
    @ConditionalOnProperty(prefix = "tinderbot.api.security", name = "enabled", havingValue = "true")
    SecurityFilterChain publicInfrastructure(HttpSecurity http) {
        return http
                .securityMatcher(
                        "/actuator/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/api-docs/**",
                        "/v3/api-docs/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(1)
    @ConditionalOnProperty(prefix = "tinderbot.api.security", name = "enabled", havingValue = "true")
    SecurityFilterChain securedApi(HttpSecurity http, ApiAuthFilter apiAuthFilter) {
        return http
                .securityMatcher("/api/v1/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(apiAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(2)
    @ConditionalOnProperty(prefix = "tinderbot.api.security", name = "enabled", havingValue = "true")
    SecurityFilterChain defaultPermitAll(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(3)
    @ConditionalOnProperty(prefix = "tinderbot.api.security", name = "enabled", havingValue = "false", matchIfMissing = true)
    SecurityFilterChain openApi(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tinderbot.api.security", name = "enabled", havingValue = "true")
    FilterRegistrationBean<ApiAuthFilter> disableApiAuthFilterServletRegistration(ApiAuthFilter apiAuthFilter) {
        FilterRegistrationBean<ApiAuthFilter> registration = new FilterRegistrationBean<>(apiAuthFilter);
        registration.setEnabled(false);
        return registration;
    }
}
