package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.security.UserDetailsImpl;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/welcome")
    public String welcome(@AuthenticationPrincipal UserDetailsImpl user, Model model) {
        model.addAttribute("username", user.getUsername());
        return "welcome";
    }

    @GetMapping("/user")
    public String userInfo(@AuthenticationPrincipal UserDetailsImpl user, Model model) {
        model.addAttribute("user", userService.getUserByName(user.getUsername()));
        return "user";
    }

    @GetMapping("/admin")
    public String allUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users/users";
    }

    @GetMapping("/add")
    public String addUser(Model model) {
        model.addAttribute("allRoles", roleService.getAllRoles());
        model.addAttribute("user", new User());
        return "users/add_user";
    }


    @PostMapping("/create")
    public String addUser(@ModelAttribute("user") User user,
                          @RequestParam(required = false) List<Integer> roleIds) {
        userService.saveUser(user, roleIds);
        return "redirect:/admin";
    }

    @GetMapping("/edit")
    public String editUser(@RequestParam int id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("allRoles", roleService.getAllRoles());
        return "users/edit";
    }

    @PostMapping("/update")
    public String updateUser(
            @ModelAttribute("user") User user,
            @RequestParam(required = false) List<Integer> roleIds) {
        userService.updateUser(user.getId(), user, roleIds);
        return "redirect:/admin";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam int id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}