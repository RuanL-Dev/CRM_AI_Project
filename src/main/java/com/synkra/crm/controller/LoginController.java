package com.synkra.crm.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "forward:/auth/login.html";
    }

    @GetMapping("/healthz")
    @ResponseBody
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/auth/csrf")
    @ResponseBody
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of(
            "token", csrfToken.getToken(),
            "parameterName", csrfToken.getParameterName(),
            "headerName", csrfToken.getHeaderName()
        );
    }
}
