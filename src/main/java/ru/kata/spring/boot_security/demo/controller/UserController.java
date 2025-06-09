package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.security.UserDetailsImpl;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public String addUser(@RequestParam String name,
                          @RequestParam String lastName,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam(required = false) List<Integer> roleIds) {

        User user = new User();
        user.setName(name);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);

        if (roleIds != null) {
            Set<Role> roles = roleIds.stream()
                    .map(roleService::findById)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        userService.saveUser(user);
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
            @RequestParam int id,
            @RequestParam String name,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) List<Integer> roleIds) {

        User user = new User();
        user.setName(name);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        if (roleIds != null) {
            Set<Role> roles = roleIds.stream()
                    .map(roleService::findById)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        } else {
            user.setRoles(new HashSet<>());
        }
        userService.updateUser(id, user);
        return "redirect:/admin";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam int id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}