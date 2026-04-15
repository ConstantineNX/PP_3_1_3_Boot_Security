package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.AdminUserService;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {


    private final AdminUserService userService;

    public AdminController(AdminUserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("users")
    public List<User> findAll() {
        return userService.findAllWithRoles();
    }

    @ModelAttribute("user")
    public User newUser() {
        return new User();
    }

    public void addAttributes(Model model, String view, User currentUser) {
        boolean isAdmin = false;
            for (Role role : currentUser.getRoles()) {
                if (role.getName().equals("ROLE_ADMIN")) {
                    isAdmin = true;
                    break;
                }
            }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("view", view);
        model.addAttribute("allRoles", userService.findAllRoles());
    }

    @GetMapping()
    public String findAllUsers(@AuthenticationPrincipal User currentUser,
                               @RequestParam(required = false) String view,
                               Model model) {
        User freshUser = userService.findUserById(currentUser.getId());
        addAttributes(model, view, freshUser);
        model.addAttribute("activeTab", "usersTable");
        if ("USER".equals(view)) {
            model.addAttribute("users",List.of(freshUser));
        }
        return "admin/adminBootstrap";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute User user, BindingResult result,
                           @RequestParam(required = false) String view,
                           @AuthenticationPrincipal User currentUser,
                           Model model) {
        if (result.hasErrors()) {
            addAttributes(model, view, currentUser);
            model.addAttribute("user", user);
            model.addAttribute("activeTab", "saveTab");
            return "admin/adminBootstrap";
        }
        try {
            userService.saveUser(user);
            return "redirect:/admin";
        } catch (IllegalArgumentException | EntityExistsException e) {
            User freshUser = userService.findUserByEmail(currentUser.getEmail());
            if (e.getMessage().contains("email") | e.getMessage().contains("exists")) {
                result.rejectValue("email", null,  "User already exists");
            } else {
                model.addAttribute("error", e.getMessage());
            }
            addAttributes(model, view, freshUser);
            model.addAttribute("user", user);
            model.addAttribute("activeTab", "saveTab");
            return "admin/adminBootstrap";
        }
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateUser(@Valid @ModelAttribute User user,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            HashMap<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().stream()
                    .filter(errorspassword -> !errorspassword.getField().equals("password"))
                    .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(errors);
            }
        }
        try {
            userService.updateUser(user.getId(), user);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
