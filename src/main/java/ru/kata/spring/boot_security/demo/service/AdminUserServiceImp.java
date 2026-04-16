package ru.kata.spring.boot_security.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.repository.UserRepository;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.*;

@Service
public class AdminUserServiceImp implements AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;


    public AdminUserServiceImp(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> findAllWithRoles() {
        return userRepository.findAllWithRoles();
    }

    @Transactional
    @Override
    public User saveUser(User user) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(user.getFirstName());
        Objects.requireNonNull(user.getEmail());
        if (user.getFirstName().trim().isEmpty() || user.getEmail().trim().isEmpty()) {
            throw new EntityNotFoundException("The user must have at least a name and email address");
        }
        if (user.getFirstName().length() < 3 || user.getFirstName().length() > 30) {
            throw new EntityNotFoundException("The first name must be between 3 and 30 characters");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EntityExistsException("The user already exists");
        }
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        userRepository.deleteById(id);
    }

    @Transactional
    @Override
    public User updateUser(Long id, User user) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(id);
        User user1 = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User with id: " + id));
        user1.setFirstName(user.getFirstName());
        user1.setLastName(user.getLastName());
        user1.setAge(user.getAge());
        user1.setCity(user.getCity());
        user1.setEmail(user.getEmail());
        user1.setPhone(user.getPhone());
        return userRepository.save(user1);
    }

    @Transactional(readOnly = true)
    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Invalid user id"));
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmailWithRoles(email).orElseThrow(() -> new EntityNotFoundException("Invalid user email"));
    }

    @Transactional
    @Override
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EntityExistsException("Пользователь с Email: " + user.getEmail() + " уже существует");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getEmail().matches("(?i)^(admin|administrator)[0-9]*@.*")) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(()-> new EntityNotFoundException("Role not found: ROLE_ADMIN"));
            user.getRoles().add(adminRole);
        }
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(()-> new EntityNotFoundException("Role not found: ROLE_USER"));
        user.getRoles().add(userRole);
        return userRepository.save(user);
    }
}
