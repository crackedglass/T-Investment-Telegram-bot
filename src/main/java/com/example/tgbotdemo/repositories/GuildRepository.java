package com.example.tgbotdemo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.tgbotdemo.domain.*;

@Repository
public interface GuildRepository extends JpaRepository<Guild, Long> {

    @Query("SELECT g FROM Guild g LEFT JOIN FETCH g.users LEFT JOIN FETCH g.cells WHERE g.name=:name")
    public Guild findByName(String name);

    @Query("SELECT g FROM Guild g LEFT JOIN FETCH g.users")
    public List<Guild> findAll();
}
