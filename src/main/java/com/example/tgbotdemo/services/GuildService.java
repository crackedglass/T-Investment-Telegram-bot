package com.example.tgbotdemo.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.tgbotdemo.repositories.*;
import com.example.tgbotdemo.domain.*;

@Service
public class GuildService {

    private GuildRepository guildRepository;

    public GuildService(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    public Guild getByName(String name) {
        return guildRepository.findByName(name);
    }

    public List<Guild> findAll() {
        return guildRepository.findAll();
    }

    public void deleteAll() {
        guildRepository.deleteAll();
    }

    public void save(Guild guild) {
        guildRepository.save(guild);
    }
}
