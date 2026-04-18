package com.synkra.crm.config;

import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

@Component
public class RuntimeSecurityGuardrails {

    private static final Set<String> WEAK_VALUES = Set.of(
        "admin",
        "change-me",
        "change-me-now",
        "password",
        "123456",
        "12345678",
        "crm_app",
        "crm_app_password"
    );

    private final Environment environment;
    private final SecurityProperties securityProperties;

    public RuntimeSecurityGuardrails(Environment environment, SecurityProperties securityProperties) {
        this.environment = environment;
        this.securityProperties = securityProperties;
    }

    @PostConstruct
    void validate() {
        if (environment.matchesProfiles("dev") || environment.matchesProfiles("test")) {
            return;
        }

        String datasourceUrl = environment.getProperty("spring.datasource.url");
        String datasourceUsername = environment.getProperty("spring.datasource.username");
        String datasourcePassword = environment.getProperty("spring.datasource.password");

        requireConfigured("spring.datasource.url", datasourceUrl);
        requireConfigured("spring.datasource.username", datasourceUsername);
        requireConfigured("spring.datasource.password", datasourcePassword);
        requireStrongSecret("spring.datasource.password", datasourcePassword, datasourceUsername);

        if (securityProperties.isBootstrapEnabled()) {
            requireConfigured("app.security.username", securityProperties.getUsername());
            requireConfigured("app.security.password", securityProperties.getPassword());
            requireStrongSecret("app.security.password", securityProperties.getPassword(), securityProperties.getUsername());
        }
    }

    private void requireConfigured(String propertyName, String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Missing required secure configuration: " + propertyName);
        }
    }

    private void requireStrongSecret(String propertyName, String secretValue, String relatedIdentifier) {
        String normalized = secretValue == null ? "" : secretValue.trim();
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (normalized.length() < securityProperties.getPasswordPolicy().getMinLength()) {
            throw new IllegalStateException(propertyName + " must have at least "
                + securityProperties.getPasswordPolicy().getMinLength() + " characters outside dev/test.");
        }
        if (WEAK_VALUES.contains(lower)) {
            throw new IllegalStateException(propertyName + " uses a blocked weak default value.");
        }
        if (StringUtils.hasText(relatedIdentifier) && normalized.equalsIgnoreCase(relatedIdentifier.trim())) {
            throw new IllegalStateException(propertyName + " must not match its paired username.");
        }
    }
}
