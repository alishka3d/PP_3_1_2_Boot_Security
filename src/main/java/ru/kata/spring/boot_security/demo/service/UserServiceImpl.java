package ru.kata.spring.boot_security.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import ru.kata.spring.boot_security.demo.security.UserDetailsImpl;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        User userAdmin = userRepository.getUserByName("admin")
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + "admin"));
        userAdmin.setPassword(passwordEncoder.encode("111"));
        userRepository.save(userAdmin);
        log.info("Создан администратор: admin / 111");
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(int id) {
        return userRepository.getById(id);
    }

    public User getUserByName(String name) {
        return userRepository.getUserByName(name).orElseThrow();
    }

    @Override
    public void saveUser(User user, List<Integer> roleIds) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            setRolesById(user, roleIds);
            user.getRoles().forEach(role -> role.getUsers().add(user));
            userRepository.save(user);
            log.info("User saved. Name: {}", user.getName());
        } catch (Exception e) {
            log.error("Error saving user: {}", e.getMessage());
        }
    }

    @Override
    public void updateUser(int id, User updatedUser, List<Integer> roleIds) {

        setRolesById(updatedUser, roleIds);

        User userFromDB = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        userFromDB.setName(updatedUser.getName());
        userFromDB.setLastName(updatedUser.getLastName());
        userFromDB.setEmail(updatedUser.getEmail());
        userFromDB.setPassword(passwordEncoder.encode(updatedUser.getPassword()));

        userFromDB.getRoles().forEach(role -> role.getUsers().remove(userFromDB));
        userFromDB.getRoles().clear();

        if (updatedUser.getRoles() != null) {
            updatedUser.getRoles().forEach(role -> {
                role.getUsers().add(userFromDB);
                userFromDB.getRoles().add(role);
            });
        }

        userRepository.save(userFromDB);
        log.info("User updated. Name: {}", userFromDB.getName());
    }

    @Override
    public void deleteUser(int id) {
        User user = userRepository.findById(id)
                .orElseThrow();
        for (Role role : user.getRoles()) {
            role.getUsers().remove(user);
        }
        user.getRoles().clear();
        userRepository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.getUserByName(username);
        if (user.isPresent()) {
            return new UserDetailsImpl(user.get());
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    private void setRolesById(User user, List<Integer> roleIds) {
        if (roleIds != null) {
            Set<Role> roles = roleIds.stream()
                    .map(roleService::findById)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        } else {
            user.setRoles(new HashSet<>());
        }
    }
}