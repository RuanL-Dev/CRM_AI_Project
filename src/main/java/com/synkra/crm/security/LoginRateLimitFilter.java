package com.synkra.crm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttemptService;

    public LoginRateLimitFilter(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod()) || !"/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (loginAttemptService.isBlocked(LoginClientKeyResolver.resolve(request))) {
            response.sendRedirect(request.getContextPath() + "/login?locked");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
