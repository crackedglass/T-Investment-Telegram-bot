package com.example.tgbotdemo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.tgbotdemo.repositories.*;
import com.example.tgbotdemo.domain.*;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getByUsername(String username) {
        String name = username;
        if (Optional.ofNullable(username).isPresent()) {
            name = username.toLowerCase();
        }
        return userRepository.findByUsername(name);
    }

    public User findByUsernameWithGuild(String username) {
        String name = username;
        if (Optional.ofNullable(username).isPresent()) {
            name = username.toLowerCase();
        }
        return userRepository.findByUsernameWithGuild(name);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public void save(User user) {
        String username = user.getUsername();
        if (Optional.ofNullable(username).isPresent()) {
            username = user.getUsername().toLowerCase();
        }
        user.setUsername(username);
        userRepository.save(user);
    }
}
