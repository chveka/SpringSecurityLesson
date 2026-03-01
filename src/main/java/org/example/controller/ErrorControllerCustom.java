package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ErrorControllerCustom implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("jakarta.servlet.error.exception");
        model.addAttribute("status", statusCode != null ? statusCode : 500);
        model.addAttribute("error", exception != null ? exception.getMessage() : "Unknown error");
        return "error";
    }

    @GetMapping("/error/403")
    public String accessDenied(Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("error", "Access Denied");
        model.addAttribute("message", "You don't have permission to access this resource");
        return "error";
    }

    @GetMapping("/error/auth")
    public String authError(@RequestParam(required = false) String message, Model model) {
        model.addAttribute("status", 401);
        model.addAttribute("error", "Authentication Error");
        model.addAttribute("message", message != null ? message : "Authentication failed");
        return "error";
    }
}