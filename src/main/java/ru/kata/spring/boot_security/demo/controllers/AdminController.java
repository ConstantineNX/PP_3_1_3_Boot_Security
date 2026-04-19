package ru.kata.spring.boot_security.demo.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.AdminUserService;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;

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

    public void addAttributes(Model model, String view, User currentUser) {
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("view", view);
        model.addAttribute("allRoles", userService.findAllRoles());
    }

    @GetMapping()
    public String findAllUsers(@AuthenticationPrincipal User currentUser,
                               @RequestParam(required = false) String view,
                               Model model) {
        User freshUser = userService.findUserById(currentUser.getId());
        model.addAttribute("user", new User());
        model.addAttribute("newUser", new User());
        addAttributes(model, view, freshUser);
        model.addAttribute("activeTab", "usersTable");
        if ("USER".equals(view)) {
            model.addAttribute("users",List.of(freshUser));
        }
        return "admin/adminBootstrap";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("newUser") User user, BindingResult result,
                           @RequestParam(required = false) String view,
                           @AuthenticationPrincipal User currentUser,
                           Model model) {
        model.addAttribute("activeTab", "saveTab");
        model.addAttribute("newUser", user);
        model.addAttribute("user", new User());
        addAttributes(model, view, currentUser);
        if (result.hasErrors()) {
            return "admin/adminBootstrap";
        }
        try {
            userService.saveUser(user);
            return "redirect:/admin";
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/adminBootstrap";
        } catch (EntityExistsException e) {
            result.rejectValue("email", null, "User already exists");
            return "admin/adminBootstrap";
        }
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }

    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute User user,
                                        BindingResult bindingResult,
                                        @RequestParam(required = false) String view,
                                        @RequestParam(required = false) Long roleId,
                                        @AuthenticationPrincipal User currentUser,
                                        Model model) throws JsonProcessingException {
        addAttributes(model, view, currentUser);
        model.addAttribute("newUser", new User());
        model.addAttribute("activeTab", "usersTable");
        if (bindingResult.hasErrors()) {
                ObjectMapper mapper = new ObjectMapper();
                String userJson = mapper.writeValueAsString(user);
                model.addAttribute("userJson", userJson);
                model.addAttribute("openEditModal", true);
                return "admin/adminBootstrap";
        }
        try {
            userService.updateUser(user.getId(), user, roleId);
            return "redirect:/admin";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("openEditModal", true);
            return "admin/adminBootstrap";
        }
    }
}
