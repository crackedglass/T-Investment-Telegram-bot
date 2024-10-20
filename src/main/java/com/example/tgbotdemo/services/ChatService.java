package com.example.tgbotdemo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import com.example.tgbotdemo.domain.Admin;
import com.example.tgbotdemo.domain.User;
import com.example.tgbotdemo.domain.statemachine.ChatStates;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ChatService {
    @Autowired
    private TelegramBot bot;

    @Autowired
    private StateMachineFactory<ChatStates, String> factory;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    private Map<String, StateMachine<ChatStates, String>> stateMachines;
    private boolean ready;

    public ChatService() {
        stateMachines = new HashMap<>();
        ready = true;
    }

    public void handleMessage(Message message) {
        log.info("Message recieved: \"" + message.text() + "\" from chat: " + message.chat().id());

        List<String> admins = adminService.getAllAdmins().stream().map(item -> item.getUsername()).toList();

        User user = userService.getByUsername(message.chat().username());

        if (user == null && admins.contains(message.chat().username())) {
            userService.save(new User(message.chat().username(), 0, null));
            user = userService.getByUsername(message.chat().username());
        } else if (user == null && !admins.contains(message.chat().username())) {
            bot.execute(new SendMessage(message.chat().id(), "Вы не зарегистрированы в турнире")
                    .replyMarkup(new ReplyKeyboardRemove()));
            return;
        }

        if (stateMachines.get(user.getUsername()) == null) {
            ChatStates state = user.getState();
            stateMachines.put(message.chat().username(), factory.getStateMachine());
            stateMachines.get(message.chat().username()).getStateMachineAccessor().doWithAllRegions(
                    action -> action.resetStateMachine(new DefaultStateMachineContext<>(state, null, null, null)));
        }

        StateMachine<ChatStates, String> sm = stateMachines.get(message.chat().username());

        sm.getExtendedState().getVariables().put("msg", message);

        if (message.document() != null && listenerService.chatHaveListener(message.chat())) {
            MessageListener listener = listenerService.popListenerFromChat(message.chat());
            listener.process(message);
            return;
        }

        if (message.text() == null)
            return;
        if (message.text().equals("/start")) {
            sm.getStateMachineAccessor().doWithAllRegions(access -> access
                    .resetStateMachine(new DefaultStateMachineContext<>(ChatStates.MAIN, "/start", null, null)));
        }

        if (ready == false) {
            listenerService.dropAllListeners();
            bot.execute(new SendMessage(message.chat().id(),
                    "Счетовод рисует новую карту, скоро снова можно будет инвестировать в клетки!"));
        }

        if (listenerService.chatHaveListener(message.chat())) {
            MessageListener listener = listenerService.popListenerFromChat(message.chat());
            listener.process(message);
        } else {
            if (message.text() != null)
                sm.sendEvent(message.text().toLowerCase());
        }

        User newUser = userService.getByUsername(message.chat().username());
        newUser.setState(sm.getState().getId());
        userService.save(newUser);
        log.info(sm.getState().toString());
    }

    public void setNotReady() {
        ready = false;
    }

    public void setReady() {
        ready = true;
    }
}
