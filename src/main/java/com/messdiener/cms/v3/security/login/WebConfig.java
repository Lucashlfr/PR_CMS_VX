package com.messdiener.cms.v3.security.login;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CustomPasswordInterceptor customPasswordInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(customPasswordInterceptor)
                .addPathPatterns("/**") // Überall prüfen
                .excludePathPatterns("/security/customPw", "/login", "/logout", "/error", "/static/**", "/", "/update", "/go/**", "/storage/file/**", "/infos","/about","/contact", "/impressum", "/static/**", "/dist/**", "/img/**", "/file/**", "/css/**", "/script/**", "/download", "/output", "/public/**", "/health", "/js/**", "/fonts/**"); // Ausnahmen
    }



}
