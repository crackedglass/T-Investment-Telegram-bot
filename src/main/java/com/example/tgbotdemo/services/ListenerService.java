package com.example.tgbotdemo.services;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pengrad.telegrambot.model.Chat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ListenerService {
    private HashMap<Chat, MessageListener> listeners;

    public ListenerService() {
        listeners = new HashMap<>();
    }

    public void pushListenerToChat(Chat chat, MessageListener listener) {
        listeners.put(chat, listener);
        log.info("Listener have set to chat: " + chat.id());
    }

    public MessageListener popListenerFromChat(Chat chat) {
        MessageListener listener = listeners.get(chat);
        listeners.remove(chat);
        log.info("Listener have removed from chat: " + chat.id());
        return listener;
    }

    public boolean chatHaveListener(Chat chat) {
        if (listeners.get(chat) == null)
            return false;
        return true;
    }
}
