package com.example.tgbotdemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.tgbotdemo.domain.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    public Admin findByUsername(String username);
}
