package com.synkra.crm.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

public class RateLimitedAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;

    public RateLimitedAuthenticationFailureHandler(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
        setDefaultFailureUrl("/login?error");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String clientKey = LoginClientKeyResolver.resolve(request);
        loginAttemptService.recordFailure(clientKey);
        if (loginAttemptService.isBlocked(clientKey)) {
            setDefaultFailureUrl("/login?locked");
        } else {
            setDefaultFailureUrl("/login?error");
        }
        super.onAuthenticationFailure(request, response, exception);
    }
}
