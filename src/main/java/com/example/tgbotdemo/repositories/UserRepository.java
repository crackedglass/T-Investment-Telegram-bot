package com.example.tgbotdemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.tgbotdemo.domain.*;;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public User findByUsername(String name);

}
