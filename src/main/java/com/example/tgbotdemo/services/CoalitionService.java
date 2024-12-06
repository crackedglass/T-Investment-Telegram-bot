package com.example.tgbotdemo.services;

import org.springframework.stereotype.Service;

import com.example.tgbotdemo.repositories.CoalitionRepository;

@Service
public class CoalitionService {

    private CoalitionRepository coalitionRepository;

    public CoalitionService(CoalitionRepository coalitionRepository) {
        this.coalitionRepository = coalitionRepository;
    }

}
