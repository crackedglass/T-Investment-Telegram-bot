package com.example.tgbotdemo.services;

import com.pengrad.telegrambot.model.Message;

@FunctionalInterface
public interface MessageListener {
    public void process(Message message);
}
