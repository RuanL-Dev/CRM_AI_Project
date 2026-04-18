package com.synkra.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String dashboard() {
        return "forward:/ui/index.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "forward:/ui/admin/index.html";
    }

    @GetMapping("/formulario")
    public String formPage() {
        return "forward:/ui/formulario/index.html";
    }
}
