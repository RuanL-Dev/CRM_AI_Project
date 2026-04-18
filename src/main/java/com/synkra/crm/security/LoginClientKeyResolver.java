package com.synkra.crm.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

final class LoginClientKeyResolver {

    private LoginClientKeyResolver() {
    }

    static String resolve(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
