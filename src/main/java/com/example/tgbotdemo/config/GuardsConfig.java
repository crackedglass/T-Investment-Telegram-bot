package com.example.tgbotdemo.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.guard.Guard;

import com.example.tgbotdemo.domain.statemachine.ChatStates;
import com.pengrad.telegrambot.model.Message;

@Configuration
public class GuardsConfig {
    private List<String> admin_list = List.of(
            "ya_qlgn",
            "Ereteik",
            "mymarichko");

    @Bean
    public Guard<ChatStates, String> adminGuard() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            if (admin_list.contains(message.chat().username()))
                return true;

            return false;
        };
    }
}