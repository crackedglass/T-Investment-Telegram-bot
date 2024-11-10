package com.example.tgbotdemo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.tgbotdemo.domain.*;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.guild WHERE u.username = :name")
    public User findByUsernameWithGuild(@Param("name") String name);

    public User findByUsername(String name);

}
