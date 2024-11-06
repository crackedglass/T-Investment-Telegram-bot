package com.example.tgbotdemo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tgbotdemo.repositories.*;
import com.example.tgbotdemo.domain.*;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User getByUsername(String username) {
        if (Optional.ofNullable(username.toLowerCase()).isPresent()) {
            username = username.toLowerCase();
        }
        return userRepository.findByUsername(username);
    }

    public User findByUsernameWithGuild(String username) {
        if (Optional.ofNullable(username.toLowerCase()).isPresent()) {
            username = username.toLowerCase();
        }
        return userRepository.findByUsernameWithGuild(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public void save(User user) {
        String username = "";
        if (Optional.ofNullable(user.getUsername().toLowerCase()).isPresent()) {
            username = user.getUsername().toLowerCase();
        }
        user.setUsername(username);
        userRepository.save(user);
    }
}
