package com.example.tgbotdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.pengrad.telegrambot.TelegramBot;

@Configuration
public class BotConfig {
    @Bean
    public TelegramBot getBot(Environment environment) {
        return new TelegramBot(environment.getProperty("TOKEN"));
    }
}
