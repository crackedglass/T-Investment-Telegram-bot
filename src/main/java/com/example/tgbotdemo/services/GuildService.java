package com.example.tgbotdemo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tgbotdemo.repositories.*;
import com.example.tgbotdemo.domain.*;

@Service
public class GuildService {
    @Autowired
    private GuildRepository guildRepository;

    public Guild getByName(String name) {
        return guildRepository.findByName(name);
    }

    public void save(Guild guild) {
        guildRepository.save(guild);
    }
}
