package com.example.tgbotdemo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.tgbotdemo.domain.*;

@Repository
public interface CellRepository extends JpaRepository<Cell, Long> {

    public List<Cell> findByLevel(int level);

    @Query("SELECT c FROM Cell c LEFT JOIN FETCH c.orders WHERE c.number=:number")
    public Cell findByNumber(int number);

    public List<Cell> findByOwnerGuild(Guild ownerGuild);

    @Query("SELECT c FROM Cell c LEFT JOIN FETCH c.ownerGuild")
    public List<Cell> findAll();
}
