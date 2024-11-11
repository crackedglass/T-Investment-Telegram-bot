package com.example.tgbotdemo.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import com.example.tgbotdemo.domain.statemachine.ChatStates;
import com.example.tgbotdemo.services.AdminService;
import com.example.tgbotdemo.services.BlockService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

@Configuration
public class GuardsConfig {
    @Autowired
    private AdminService adminService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private TelegramBot bot;

    private Keyboard menuKeyboard = new ReplyKeyboardMarkup(
            new KeyboardButton[][] {
                    { new KeyboardButton("Сколько у меня серебра") },
                    { new KeyboardButton("Сколько серебра у моей гильдии") },
                    { new KeyboardButton("Инвестировать в территорию") },
                    { new KeyboardButton("Мои инвестиции в территории") },
                    { new KeyboardButton("Сколько гильдии инвестировали в территории") }
            });

    @Bean
    public Guard<ChatStates, String> adminGuard() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            List<String> admins = adminService.getAllAdmins().stream().map(i -> i.getUsername()).toList();

            String username = message.chat().username();
            if (username != null)
                username = username.toLowerCase();
            if (admins.contains(username))
                return true;

            return false;
        };
    }

    @SuppressWarnings("deprecation")
    @Bean
    public Guard<ChatStates, String> isBlocked() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            if (blockService.isBlocked()) {
                bot.execute(
                        new SendMessage(message.chat().id(),
                                "Счетовод рисует новую карту. Скоро вложения снова станут доступны")
                                .replyMarkup(menuKeyboard));

                context.getStateMachine().getStateMachineAccessor().doWithAllRegions(access -> access
                        .resetStateMachine(new DefaultStateMachineContext<>(ChatStates.MAIN, null, null, null)));

                return false;
            }

            return true;
        };
    }
}