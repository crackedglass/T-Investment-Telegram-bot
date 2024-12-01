package com.example.tgbotdemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.tgbotdemo.domain.Coalition;

@Repository
public interface CoalitionRepository extends JpaRepository<Coalition, Long> {

}
