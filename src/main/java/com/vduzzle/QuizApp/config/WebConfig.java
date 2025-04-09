package com.vduzzle.QuizApp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Engedélyezd az összes útvonalat
                .allowedOrigins("http://localhost:3000") // Engedélyezd a React alkalmazás eredetét
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Engedélyezd a HTTP metódusokat
                .allowedHeaders("*") // Engedélyezd az összes fejlécet
                .allowCredentials(true); // Engedélyezd a hitelesítő adatokat (pl. sütik)
    }
}