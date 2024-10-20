package com.example.tgbotdemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

import com.example.tgbotdemo.domain.statemachine.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<ChatStates, String> {
    @Autowired
    private GuardsConfig guardsConfig;
    @Autowired
    private MainActionsConfig mainActionsConfig;
    @Autowired
    private AdminActionsConfig adminActionsConfig;

    @Override
    public void configure(StateMachineStateConfigurer<ChatStates, String> states)
            throws Exception {
        states
                .withStates()
                .initial(ChatStates.MAIN)
                .states(EnumSet.allOf(ChatStates.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ChatStates, String> transitions)
            throws Exception {
        transitions
                // From MAIN state
                .withInternal()
                .source(ChatStates.MAIN).event("/start").action(mainActionsConfig.start())
                .and()
                .withInternal()
                .source(ChatStates.MAIN).event("сколько серебра у моей гильдии")
                .action(mainActionsConfig.getGuildMoney())
                .and()
                .withInternal()
                .source(ChatStates.MAIN).event("сколько у меня серебра").action(mainActionsConfig.getUserMoney())
                .and()
                .withExternal()
                .source(ChatStates.MAIN).target(ChatStates.ORDER_ASKING_CELL).event("инвестировать в территорию")
                .action(mainActionsConfig.orderFirstStep())
                .and()
                .withInternal()
                .source(ChatStates.MAIN).event("мои инвестиции в территории")
                .action(mainActionsConfig.getUserOrders())
                .and()
                .withExternal()
                .source(ChatStates.MAIN).target(ChatStates.INFO_ASKING_CELL)
                .event("сколько гильдии инвестировали в территории").action(mainActionsConfig.getGuildOrders())
                .and()
                .withExternal()
                .source(ChatStates.MAIN).target(ChatStates.ADMIN).event("/admin").guard(guardsConfig.adminGuard())
                .action(adminActionsConfig.sendAdminMenu())
                // From ORDER_ASKING_CELL state
                .and()
                .withExternal()
                .source(ChatStates.ORDER_ASKING_CELL).target(ChatStates.MAIN).event("BACK_TO_MENU")
                .and()
                .withExternal()
                .source(ChatStates.ORDER_ASKING_CELL).target(ChatStates.ORDER_ASKING_AMOUNT)
                .event("NEXT").action(mainActionsConfig.orderSecondStep())
                // From ORDER_ASKING_AMOUNT state
                .and()
                .withExternal()
                .source(ChatStates.ORDER_ASKING_AMOUNT).target(ChatStates.MAIN).event("BACK_TO_MENU")
                // From ASKING_ACTION state
                .and()
                .withExternal()
                .source(ChatStates.ASKING_ACTION).target(ChatStates.MAIN).event("BACK_TO_MENU")
                .and()
                .withExternal()
                .source(ChatStates.ASKING_ACTION).target(ChatStates.ASKING_TO_DELETE)
                .event("убрать вложение")
                // From ASKING_TO_DELETE state
                .and()
                .withExternal()
                .source(ChatStates.ASKING_TO_DELETE).target(ChatStates.ASKING_ACTION)
                .event("WRONG_ORDER_TO_DELETE")
                .and()
                .withExternal()
                .source(ChatStates.ASKING_TO_DELETE).target(ChatStates.ASKING_ACTION)
                .event("SUCCESSFULLY_DELETED")
                // From INFO_ASKING_CELL state
                .and()
                .withExternal()
                .source(ChatStates.INFO_ASKING_CELL).target(ChatStates.MAIN).event("BACK_TO_MENU")
                // From ADMIN
                .and()
                .withExternal()
                .source(ChatStates.ADMIN).target(ChatStates.MAIN).event("выйти в главное меню")
                .action(mainActionsConfig.sendMenu())
                .and()
                .withInternal()
                .source(ChatStates.ADMIN).event("добавить серебро пользователям")
                .action(adminActionsConfig.addMoneyToUsers())
                .and()
                .withInternal()
                .source(ChatStates.ADMIN).event("загрузить таблицу с пользователями")
                .action(adminActionsConfig.loadUsers())
                .and()
                .withInternal()
                .source(ChatStates.ADMIN).event("остановить вложения").action(adminActionsConfig.stopTrades()); // TODO
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<ChatStates, String> config) throws Exception {
        config.withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    @Bean
    public StateMachineListener<ChatStates, String> listener() {
        return new StateMachineListenerAdapter<ChatStates, String>() {
            @Override
            public void stateChanged(State<ChatStates, String> from, State<ChatStates, String> to) {
                log.info("State changed to " + to.getId());
            }
        };
    }
}
