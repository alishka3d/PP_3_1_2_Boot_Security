package ru.kata.spring.boot_security.demo.service;

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
        if (userRepository.getUserByName("admin").isEmpty()) {
            Role adminRole = roleService.getRoleByAuthority("ROLE_ADMIN");
            Role userRole = roleService.getRoleByAuthority("ROLE_USER");
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setAuthority("ROLE_ADMIN");
                roleService.saveRole(adminRole);
            }
            if (userRole == null) {
                System.err.println("role_user");
                userRole = new Role();
                userRole.setAuthority("ROLE_USER");
                roleService.saveRole(userRole);
            }
            // Создаём пользователя
            User admin = new User();
            admin.setName("admin");
            admin.setLastName("admin");
            admin.setEmail("admin@admin.com");
            admin.setPassword("111"); // Шифруем пароль
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            admin.setRoles(roles);
            saveUser(admin, new ArrayList<>());
            System.err.println("Создан администратор: admin / 111");
        }
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
        } catch (Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
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