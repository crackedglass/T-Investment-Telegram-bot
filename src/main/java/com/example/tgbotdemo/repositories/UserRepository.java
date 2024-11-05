package com.example.tgbotdemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.tgbotdemo.domain.*;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.guild WHERE u.username = :name ")
    public User findByUsernameWithGuild(String name);

    public User findByUsername(String name);

}
