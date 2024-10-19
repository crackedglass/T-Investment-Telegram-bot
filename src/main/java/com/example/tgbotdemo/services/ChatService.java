package com.example.tgbotdemo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import com.example.tgbotdemo.domain.User;
import com.example.tgbotdemo.domain.statemachine.ChatStates;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
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

    private Map<String, StateMachine<ChatStates, String>> stateMachines;

    private List<String> admin_list = List.of(
            "ya_qlgn",
            "Ereteik",
            "mymarichko");

    public ChatService() {
        stateMachines = new HashMap<>();
    }

    public void handleMessage(Message message) {
        User user = userService.getByUsername(message.chat().username());

        log.info("Message recieved: \"" + message.text() + "\" from chat: " + message.chat().id());
        if (message.text() != null)
            if (message.text().equals("/start")) {
                log.info("Start detected");
                if (user == null) {
                    bot.execute(new SendMessage(message.chat().id(), "Вы не зарегистрированы в турнире"));
                    return;
                }

                if (listenerService.chatHaveListener(message.chat()))
                    listenerService.popListenerFromChat(message.chat());

                stateMachines.put(user.getUsername(), factory.getStateMachine());
                stateMachines.get(user.getUsername()).sendEvent("AUTH_APPROVED");
            } else if (message.text().equals("/admin")) {
                if (admin_list.contains(message.chat().username())) {
                    stateMachines.put(message.chat().username(), factory.getStateMachine());
                    stateMachines.get(message.chat().username()).sendEvent("");
                }
            }

        if (user == null)
            return;
        ChatStates state = user.getState();

        if (stateMachines.get(message.chat().username()) == null) {
            stateMachines.put(message.chat().username(), factory.getStateMachine());
            stateMachines.get(message.chat().username()).getStateMachineAccessor().doWithAllRegions(
                    action -> action.resetStateMachine(new DefaultStateMachineContext<>(state, null, null, null)));
        }
        StateMachine<ChatStates, String> sm = stateMachines.get(message.chat().username());

        sm.getExtendedState().getVariables().put("msg", message);
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

}
