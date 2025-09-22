// cms-feature-auth/src/main/java/com/messdiener/cms/auth/security/login/WebConfig.java
package com.messdiener.cms.auth.security.login;

import com.messdiener.cms.domain.auth.AuthContextPort;
import com.messdiener.cms.domain.person.PersonPasswordQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthContextPort authContextPort;
    private final PersonPasswordQueryPort personPasswordQueryPort;

    @Bean
    public CustomPasswordInterceptor customPasswordInterceptor() {
        return new CustomPasswordInterceptor(authContextPort, personPasswordQueryPort);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(customPasswordInterceptor())
                .order(Ordered.LOWEST_PRECEDENCE) // hinter andere Interceptors
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // Auth & PW-Setup
                        "/login", "/login-error", "/logout", "/auth/changePassword", "/security/customPw",
                        // öffentliche & statische Bereiche
                        "/", "/update", "/infos", "/about", "/contact", "/impressum", "/public/**",
                        "/static/**", "/dist/**", "/img/**", "/file/**", "/css/**", "/script/**", "/js/**", "/fonts/**",
                        "/favicon.ico", "/error",
                        // health & diverse
                        "/health",
                        // API (niemals auf HTML-Redirects schicken)
                        "/api/**"
                );
    }
}
