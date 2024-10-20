package com.example.tgbotdemo.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.guard.Guard;

import com.example.tgbotdemo.domain.statemachine.ChatStates;
import com.example.tgbotdemo.services.AdminService;
import com.pengrad.telegrambot.model.Message;

@Configuration
public class GuardsConfig {
    @Autowired
    private AdminService adminService;

    @Bean
    public Guard<ChatStates, String> adminGuard() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            List<String> admins = adminService.getAllAdmins().stream().map(i -> i.getUsername()).toList();

            if (admins.contains(message.chat().username()))
                return true;

            return false;
        };
    }
}