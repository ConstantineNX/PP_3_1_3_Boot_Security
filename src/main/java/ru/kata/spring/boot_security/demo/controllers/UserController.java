package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.AdminUserService;

@Controller
public class UserController {

    private final AdminUserService userService;

    public UserController(AdminUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public String profileUser(@AuthenticationPrincipal User user,
                              Model model) {
        User user1 = userService.findUserByEmail(user.getEmail());
        model.addAttribute("currentUser", user);
        model.addAttribute("view", "USER" );
        model.addAttribute("user", user);
        model.addAttribute("users", user1);
        model.addAttribute("activeTab", "usersTable");
        model.addAttribute("newUser", new User());
        model.addAttribute("allRoles", userService.findAllRoles());
        return "admin/adminBootstrap";
    }
}
