package com.synkra.crm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   @Value("${app.security.dev-h2-console-enabled:false}") boolean devH2ConsoleEnabled) throws Exception {
        http
            .authorizeHttpRequests(auth -> {
                if (devH2ConsoleEnabled) {
                    auth.requestMatchers("/h2-console/**").permitAll();
                }
                auth.requestMatchers("/ui/**", "/css/**", "/js/**").authenticated()
                    .requestMatchers("/", "/api/**").authenticated()
                    .anyRequest().authenticated();
            })
            .httpBasic(Customizer.withDefaults())
            .formLogin(Customizer.withDefaults())
            .csrf(csrf -> {
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
                if (devH2ConsoleEnabled) {
                    csrf.ignoringRequestMatchers("/h2-console/**");
                }
            })
            .headers(headers -> {
                if (devH2ConsoleEnabled) {
                    headers.frameOptions(frame -> frame.sameOrigin());
                }
            });

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(
        @Value("${app.security.username}") String username,
        @Value("${app.security.password}") String password,
        PasswordEncoder passwordEncoder
    ) {
        UserDetails user = User.withUsername(username)
            .password(passwordEncoder.encode(password))
            .roles("CRM_USER")
            .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
