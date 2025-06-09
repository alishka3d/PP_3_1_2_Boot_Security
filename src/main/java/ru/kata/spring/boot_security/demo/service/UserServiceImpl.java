package ru.kata.spring.boot_security.demo.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import ru.kata.spring.boot_security.demo.security.UserDetailsImpl;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    UserRepository userRepository;
    RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
    public void saveUser(User user) {
        try {
            user.getRoles().forEach(role -> role.getUsers().add(user));
            userRepository.save(user);
        } catch (Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    @Override
    public void updateUser(int id, User updatedUser) {
        User userFromDB = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        userFromDB.setName(updatedUser.getName());
        userFromDB.setLastName(updatedUser.getLastName());
        userFromDB.setEmail(updatedUser.getEmail());
        userFromDB.setPassword(updatedUser.getPassword());

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
}