package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();
    User getUserById(int id);
    User getUserByName(String name);
    void saveUser(User user, List<Integer> roleIds);
    void updateUser(int id, User user, List<Integer> roleIds);
    void deleteUser(int id);
}
