package com.synkra.crm.config;

import com.synkra.crm.model.AppUser;
import com.synkra.crm.repository.AppUserRepository;
import com.synkra.crm.security.LoginAttemptService;
import com.synkra.crm.security.LoginRateLimitFilter;
import com.synkra.crm.security.RateLimitedAuthenticationFailureHandler;
import com.synkra.crm.security.RateLimitedAuthenticationSuccessHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SecurityProperties securityProperties,
                                                   LoginRateLimitFilter loginRateLimitFilter,
                                                   RateLimitedAuthenticationSuccessHandler successHandler,
                                                   RateLimitedAuthenticationFailureHandler failureHandler) throws Exception {
        boolean devH2ConsoleEnabled = securityProperties.isDevH2ConsoleEnabled();
        http
            .authorizeHttpRequests(auth -> {
                if (devH2ConsoleEnabled) {
                    auth.requestMatchers("/h2-console/**").permitAll();
                }
                auth.requestMatchers("/login", "/healthz", "/auth/**", "/formulario", "/formulario/**", "/api/public/**").permitAll()
                    .requestMatchers("/ui/formulario/**").permitAll()
                    .requestMatchers("/ui/**", "/css/**", "/js/**", "/").hasRole("CRM_USER")
                    .requestMatchers("/api/**").hasRole("CRM_USER")
                    .anyRequest().authenticated();
            })
            .exceptionHandling(exceptions -> exceptions
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")
                )
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .logoutSuccessUrl("/login?logout")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login?expired")
                .sessionFixation(sessionFixation -> sessionFixation.changeSessionId())
            )
            .csrf(csrf -> {
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
                csrf.ignoringRequestMatchers("/api/public/**");
                if (devH2ConsoleEnabled) {
                    csrf.ignoringRequestMatchers("/h2-console/**");
                }
            })
            .headers(headers -> {
                headers.contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data:; " +
                        "font-src 'self' data:; " +
                        "connect-src 'self'; " +
                        "object-src 'none'; " +
                        "base-uri 'self'; " +
                        "frame-ancestors 'none'; " +
                        "form-action 'self'"
                ));
                headers.referrerPolicy(referrer -> referrer.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                ));
                headers.permissionsPolicy(policy -> policy.policy("camera=(), geolocation=(), microphone=(), payment=(), usb=()"));
                headers.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000));
                if (devH2ConsoleEnabled) {
                    headers.frameOptions(frame -> frame.sameOrigin());
                } else {
                    headers.frameOptions(frame -> frame.deny());
                }
            });

        http.addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(AppUserRepository appUserRepository) {
        return username -> appUserRepository.findByUsername(username)
            .filter(AppUser::isEnabled)
            .map(this::toUserDetails)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public LoginRateLimitFilter loginRateLimitFilter(LoginAttemptService loginAttemptService) {
        return new LoginRateLimitFilter(loginAttemptService);
    }

    @Bean
    public RateLimitedAuthenticationFailureHandler rateLimitedAuthenticationFailureHandler(LoginAttemptService loginAttemptService) {
        return new RateLimitedAuthenticationFailureHandler(loginAttemptService);
    }

    @Bean
    public RateLimitedAuthenticationSuccessHandler rateLimitedAuthenticationSuccessHandler(LoginAttemptService loginAttemptService) {
        return new RateLimitedAuthenticationSuccessHandler(loginAttemptService);
    }

    private UserDetails toUserDetails(AppUser appUser) {
        return org.springframework.security.core.userdetails.User.withUsername(appUser.getUsername())
            .password(appUser.getPasswordHash())
            .roles(appUser.rolesArray())
            .disabled(!appUser.isEnabled())
            .build();
    }
}
