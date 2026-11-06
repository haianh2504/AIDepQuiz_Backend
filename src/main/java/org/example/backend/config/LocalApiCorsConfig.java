package org.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LocalApiCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOrigins("http://localhost:3000","https://ai-dep-quiz-frontend.vercel.app")
                .allowedMethods("GET", "POST")
                .allowedHeaders("Accept", "Content-Type")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
