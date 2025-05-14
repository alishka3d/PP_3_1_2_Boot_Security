package ru.kata.spring.boot_security.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> getUserByName(String name);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.name = :name")
    Optional<User> getUserByNameWithRole(String name);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.id = :id")
    User findByIdWithRoles(Long id);
}
