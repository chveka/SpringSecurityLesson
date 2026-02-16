package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FirstTaskController {
    @GetMapping("/home")
    public String home() {
        return "Это публичная страница - доступна всем";
    }

    @GetMapping("/private")
    public String privatePage() {
        return "Это защищенная страница - требует логина";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "Административная страница";
    }
}