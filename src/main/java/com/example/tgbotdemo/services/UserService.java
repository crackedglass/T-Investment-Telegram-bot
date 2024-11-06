package com.example.tgbotdemo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tgbotdemo.repositories.*;
import com.example.tgbotdemo.domain.*;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User getByUsername(String username) {
        return userRepository.findByUsername(username.toLowerCase());
    }

    public User findByUsernameWithGuild(String username) {
        return userRepository.findByUsernameWithGuild(username.toLowerCase());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public void save(User user) {
        user.setUsername(user.getUsername().toLowerCase());
        userRepository.save(user);
    }
}
