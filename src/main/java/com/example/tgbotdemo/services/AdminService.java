package com.example.tgbotdemo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.tgbotdemo.domain.Admin;
import com.example.tgbotdemo.repositories.AdminRepository;

@Service
public class AdminService {

    private AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Admin findByName(String name) {
        String username = name;
        if (Optional.ofNullable(username).isPresent()) {
            username = username.toLowerCase();
        }
        return adminRepository.findByUsername(username);
    }

    public void save(Admin admin) {
        String username = "";
        if (Optional.ofNullable(admin).isPresent()) {
            if (Optional.ofNullable(admin.getUsername()).isPresent())
                username = admin.getUsername().toLowerCase();
        }
        admin.setUsername(username);
        adminRepository.save(admin);
    }
}
