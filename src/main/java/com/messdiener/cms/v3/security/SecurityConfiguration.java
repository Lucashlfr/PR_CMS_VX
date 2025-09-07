package com.messdiener.cms.v3.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.logging.Logger;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private static final Logger LOGGER = Logger.getLogger(SecurityConfiguration.class.getName());

    // Inject unser PersistentTokenRepository (PersistentLoginService)
    private final PersistentTokenRepository persistentTokenRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        return new InMemoryUserDetailsManager(); // falls eigener UserDetailsService vorhanden, hier zurückgeben
    }

    /** Zentrales RememberMeServices (alwaysRemember, Cookie: cmslogin) */
    @Bean
    public RememberMeServices rememberMeServices(UserDetailsService userDetailsService) {
        String key = "0a1aeffca06a0793b5da73c53fd2a0e35418ca322df7a03b64c8e8f06c0eea6ea0d9a2a7b7cac6fecf3408de2ae9430ff715";
        PersistentTokenBasedRememberMeServices svc =
                new PersistentTokenBasedRememberMeServices(key, userDetailsService, persistentTokenRepository);
        svc.setAlwaysRemember(true);               // kein Button nötig
        svc.setCookieName("cmslogin");             // gewünschter Cookie-Name
        svc.setTokenValiditySeconds(Integer.MAX_VALUE); // quasi "nie" ablaufen
        svc.setUseSecureCookie(true);              // nur über HTTPS
        return svc;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiAuthChain(HttpSecurity http, RememberMeServices rememberMeServices) throws Exception {
        var sessionRepo = new HttpSessionSecurityContextRepository();

        http
                .securityMatcher("/api/auth/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(req ->
                        req.getRequestURI() != null && req.getRequestURI().startsWith("/api/auth/")
                ))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .securityContext(sc -> sc.securityContextRepository(sessionRepo))

                // Remember-Me auch für die API-Kette aktivieren
                .rememberMe(r -> r.rememberMeServices(rememberMeServices))

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .addLogoutHandler((request, response, authentication) -> {
                            var session = request.getSession(false);
                            if (session != null) session.invalidate();

                            // Remember-Me-Cookie löschen (cmslogin)
                            Cookie rm = new Cookie("cmslogin", "");
                            rm.setPath("/");
                            rm.setMaxAge(0);
                            rm.setHttpOnly(true);
                            rm.setSecure(true);
                            response.addCookie(rm);

                            // Bestehendes App-Session-Cookie löschen
                            Cookie cookie = new Cookie("CentralManagementSystem_vX", "");
                            cookie.setPath("/");
                            cookie.setMaxAge(0);
                            cookie.setHttpOnly(true);
                            cookie.setSecure(true);
                            response.addCookie(cookie);
                        })
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
                        })
                        .deleteCookies("CentralManagementSystem_vX", "cmslogin")
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain webSecurity(HttpSecurity http, RememberMeServices rememberMeServices) throws Exception {

        http
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/"))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .requiresChannel(channel -> channel.anyRequest().requiresSecure())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/", "/update", "/go/**", "/storage/file/**", "/infos", "/about", "/contact", "/impressum",
                                "/static/**", "/dist/**", "/img/**", "/file/**", "/css/**", "/script/**", "/download", "/output", "/public/**", "/health",
                                "/js/**", "/fonts/**").permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/finance/file", "/public/**", "/register/**", "/error", "/favicon.ico").permitAll()
                        .requestMatchers("/application/save").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .failureUrl("/login?error")
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard")
                        .permitAll())

                // Persistent Remember-Me ohne Checkbox (alwaysRemember)
                .rememberMe(r -> r.rememberMeServices(rememberMeServices))

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?success")
                        .addLogoutHandler(new SecurityContextLogoutHandler())
                        .deleteCookies("cmslogin") // Remember-Me-Cookie bei Logout löschen
                )
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
