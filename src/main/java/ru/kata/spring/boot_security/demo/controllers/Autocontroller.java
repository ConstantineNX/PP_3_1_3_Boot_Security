package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.AdminUserService;
import javax.persistence.EntityExistsException;
import javax.validation.Valid;

@Controller
public class Autocontroller {
    private final AdminUserService adminUserService;
    private final AuthenticationManager authenticationManager;

    public Autocontroller(AdminUserService adminUserService, AuthenticationManager authenticationManager) {
        this.adminUserService = adminUserService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register-form";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register-form";
        }
        if (user.getPassword() == null || user.getPassword().isBlank() || user.getPassword().length() < 3) {
            model.addAttribute("error", "Please enter your password");
            return "register-form";
        }
       try {
           String rawPassword = user.getPassword();
           User user1 = adminUserService.registerUser(user);
           UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user1.getEmail(), rawPassword);
           Authentication authResult = authenticationManager.authenticate(auth);
           SecurityContextHolder.getContext().setAuthentication(authResult);
           if (user1.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
               return "redirect:/admin";
           } else {
               return "redirect:/user";
           }
       } catch (EntityExistsException e) {
           model.addAttribute("error", e.getMessage());
           return "register-form";
       }
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login-form";
    }
}
