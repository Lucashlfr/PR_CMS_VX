package com.messdiener.cms.v3.security;

import com.messdiener.cms.v3.shared.cache.Cache;
import lombok.Getter;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.logging.Logger;

@Configuration
public class SecurityConfiguration {

    private static final Logger LOGGER = Logger.getLogger("Manager.SecurityConfiguration");

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        Cache.setPasswordEncoder(bCryptPasswordEncoder);
        return bCryptPasswordEncoder;
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        return Cache.getUserDetailsManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login")
        );

        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        http.requiresChannel(channel -> channel.anyRequest().requiresSecure())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/static/**", "/dist/**", "/img/**", "/css/**", "/script/**", "/download", "/output").permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/finance/file", "/public/**", "/register/**", "/error", "/favicon.ico").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .failureUrl("/login?error")
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?success")
                        .addLogoutHandler(new SecurityContextLogoutHandler())
                )
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
