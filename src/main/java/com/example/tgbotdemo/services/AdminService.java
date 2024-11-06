package com.example.tgbotdemo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tgbotdemo.domain.Admin;
import com.example.tgbotdemo.repositories.AdminRepository;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public void save(Admin admin) {
        String username = "";
        if (Optional.ofNullable(admin.getUsername().toLowerCase()).isPresent()) {
            username = admin.getUsername().toLowerCase();
        }
        admin.setUsername(username);
        adminRepository.save(admin);
    }
}
