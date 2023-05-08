package com.example.getreadyauction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    public void addCorsMappings(final CorsRegistry registry){
        registry.addMapping("/**")
                .allowedOrigins("*")
                .exposedHeaders("Authorization");
    }
}