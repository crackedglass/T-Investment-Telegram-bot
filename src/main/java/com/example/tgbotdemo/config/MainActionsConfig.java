package com.example.tgbotdemo.config;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;

import com.example.tgbotdemo.domain.Cell;
import com.example.tgbotdemo.domain.Guild;
import com.example.tgbotdemo.domain.Order;
import com.example.tgbotdemo.domain.User;
import com.example.tgbotdemo.domain.statemachine.ChatStates;
import com.example.tgbotdemo.services.CellService;
import com.example.tgbotdemo.services.GuildService;
import com.example.tgbotdemo.services.ListenerService;
import com.example.tgbotdemo.services.OrderService;
import com.example.tgbotdemo.services.UserService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MainActionsConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private GuildService guildService;

    @Autowired
    private CellService cellService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ListenerService listenerService;

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
    public Action<ChatStates, String> start() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");
            bot.execute(new SendMessage(message.chat().id(), "*Да начнется битва!*\r\n" + //
                    "\r\n" + //
                    "Привет и добро пожаловать в гильдию! Вам предстоит сложная задача — бороться за контроль над территориями. В этом поможет бот.\r\n"
                    + //
                    "\r\n" + //
                    "Здесь вы сможете узнать, сколько серебра у вас и вашей гильдии, вложиться в территории и следить, чтобы никто не увел ваш участок из-под носа.\r\n"
                    + //
                    "\r\n" + //
                    "").parseMode(ParseMode.Markdown).replyMarkup(menuKeyboard));
        };
    }

    @Bean
    public Action<ChatStates, String> declined() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");
            bot.execute(new SendMessage(message.chat().id(), "Вы не зарегистрированы в турнире"));
            log.info("declined Bean called");
        };
    }

    @Bean
    public Action<ChatStates, String> sendMenu() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");
            bot.execute(new SendMessage(message.chat().id(), "Вы в главном меню").replyMarkup(menuKeyboard));
        };
    };

    @Bean
    public Action<ChatStates, String> getGuildMoney() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");
            Long chatId = message.chat().id();
            String username = message.chat().username().toLowerCase();
            User user = userService.getByUsername(username);
            log.info(user.toString());
            if (Optional.ofNullable(user).isPresent()) {

                Guild guild = guildService.getByName(user.getGuild().getName());

                List<String> toDisplay = guild
                        .getUsers()
                        .stream()
                        .sorted((a, b) -> a.getMoney() < b.getMoney() ? 1 : -1)
                        .map(y -> " • " + y.getUsername() + " - " + String.valueOf(y.getMoney()))
                        .limit(10)
                        .toList();

                int sum = guild.getUsers().stream().mapToInt(u -> u.getMoney()).sum();

                String text = "Твоя гильдия заработала " + sum + " серебра\nТоп 10 твоих соратников:\n"
                        + String.join("\n", toDisplay);

                bot.execute(new SendMessage(chatId, text));
            }
        };
    }

    @Bean
    public Action<ChatStates, String> getUserMoney() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            Long chatId = message.chat().id();
            String username = message.chat().username();
            User user = userService.getByUsername(username);
            if (Optional.ofNullable(user).isPresent()) {
                int money = user.getMoney();
                bot.execute(new SendMessage(chatId, String.format("У вас %d серебра", money)));
            }
        };
    }

    @SuppressWarnings("deprecation")
    @Bean
    public Action<ChatStates, String> orderFirstStep() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");
            StateMachine<ChatStates, String> sm = context.getStateMachine();
            try {
                java.io.File file = new java.io.File("resources/images/map.jpg");
                bot.execute(new SendPhoto(message.chat().id(), file)
                        .caption("Напишите номер территории, в которую хотите вложиться")
                        .replyMarkup(new ReplyKeyboardRemove()));
            } catch (Exception e) {
                log.error("Hex map not found", e);
                sm.sendEvent("BACK_TO_MENU");
                return;
            }
            listenerService.pushListenerToChat(message.chat(), m -> {
                try {
                    int cellNumber = Integer.parseInt(m.text());
                    if (cellNumber < 1 || cellNumber > 19) {
                        bot.execute(new SendMessage(message.chat().id(),
                                "Вы добрались до края света, дальше живут драконы.\n" + //
                                        "\n" + //
                                        "Напишите правильный номер территории и возвращайтесь.")
                                .replyMarkup(menuKeyboard));
                        sm.sendEvent("BACK_TO_MENU");
                        return;
                    }

                    List<Integer> available = cellService
                            .getAvailableCellsNumbersByUsername(m.chat().username());
                    List<Integer> owned = guildService
                            .getByName(userService.findByUsernameWithGuild(m.chat().username()).getGuild().getName())
                            .getCells()
                            .stream().mapToInt(i -> i.getNumber()).boxed().toList();

                    if (owned.contains(cellNumber)) {
                        bot.execute(new SendMessage(m.chat().id(),
                                "Это ваша территория")
                                .replyMarkup(menuKeyboard));
                        sm.sendEvent("BACK_TO_MENU");
                    } else if (available.contains(cellNumber)) {
                        context.getExtendedState().getVariables().put("cell", cellNumber);
                        sm.sendEvent("NEXT");
                        return;
                    } else {
                        bot.execute(new SendMessage(m.chat().id(),
                                "Вы не можете вложиться в эту территорию.\n\nУбедитесь, что ввели правильный номер и ваша гильдия контролирует соседние участки")
                                .replyMarkup(menuKeyboard));
                        sm.sendEvent("BACK_TO_MENU");
                    }
                } catch (NumberFormatException e) {
                    sm.sendEvent("BACK_TO_MENU");
                    bot.execute(new SendMessage(message.chat().id(),
                            "Вы добрались до края света, дальше живут драконы.\n" + //
                                    "\n" + //
                                    "Напишите правильный номер территории и возвращайтесь.")
                            .replyMarkup(menuKeyboard));
                }
            });
        };
    }

    @SuppressWarnings("deprecation")
    @Bean
    public Action<ChatStates, String> orderSecondStep() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");
            int cell_number = (Integer) context.getExtendedState().getVariables().get("cell");
            StateMachine<ChatStates, String> sm = context.getStateMachine();

            User u = userService.getByUsername(message.chat().username());
            bot.execute(new SendMessage(message.chat().id(),
                    "У вас " + u.getMoney() + " серебра. \nСколько хотите вложить?"));

            listenerService.pushListenerToChat(message.chat(), m -> {
                try {
                    int amount = Integer.parseInt(m.text());
                    User user = userService.getByUsername(m.chat().username());
                    if (amount >= 0 && amount <= user.getMoney()) {
                        Cell cell = cellService.getByNumber(cell_number);
                        orderService.create(user, cell, amount);
                        bot.execute(new SendMessage(m.chat().id(),
                                "Счетовод записал ваше вложение!")
                                .replyMarkup(menuKeyboard));
                    } else if (amount > user.getMoney()) {
                        bot.execute(new SendMessage(m.chat().id(),
                                "Не хватает монет.\n\nСовершите пару доходных сделок — и мы в расчете. Заодно посмотрим, как много серебра сможете заработать. ")
                                .replyMarkup(menuKeyboard));
                    } else {
                        bot.execute(new SendMessage(m.chat().id(), "Сумма вложения должна быть больше 0")
                                .replyMarkup(menuKeyboard));
                    }

                } catch (NumberFormatException e) {
                    bot.execute(new SendMessage(m.chat().id(), "Сумма вложения должна быть числом")
                            .replyMarkup(menuKeyboard));
                }
                sm.sendEvent("BACK_TO_MENU");
            });
        };
    }

    @SuppressWarnings("deprecation")
    @Bean
    public Action<ChatStates, String> getUserOrders() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");
            StateMachine<ChatStates, String> sm = context.getStateMachine();

            List<Order> orders = orderService.getOrdersByUsername(message.chat().username());

            if (orders.size() == 0) {
                bot.execute(new SendMessage(message.chat().id(),
                        "Вы еще не вкладывали серебро в территории.\n\nИнвестируйте и помогайте своей гильдии продвигаться по карте и зарабатывать серебро.")
                        .replyMarkup(menuKeyboard));
            } else {
                StringBuilder builder = new StringBuilder("Вы вложили:\n\n");
                orders.sort((a, b) -> a.getCell().getNumber() > b.getCell().getNumber() ? 1 : -1);
                for (Order order : orders) {
                    builder.append(" • В территорию " + order.getCell().getNumber() + " - "
                            + order.getAmount()
                            + "\n");
                }
                bot.execute(new SendMessage(message.chat().id(), builder.toString()).replyMarkup(menuKeyboard));
            }

            sm.sendEvent("BACK_TO_MENU");
        };
    }

    @SuppressWarnings("deprecation")
    @Bean
    public Action<ChatStates, String> getGuildOrders() {
        return context -> {

            Message message = (Message) context.getExtendedState().getVariables().get("msg");
            StateMachine<ChatStates, String> sm = context.getStateMachine();
            try {
                java.io.File file = new java.io.File("resources/images/map.jpg");
                bot.execute(new SendPhoto(message.chat().id(), file)
                        .caption(
                                "Напишите номер территории, чтобы узнать, сколько ваша и другие гильдии вложили в нее серебра")
                        .replyMarkup(new ReplyKeyboardRemove()));
            } catch (Exception e) {
                log.error("Hex map not found", e);
                sm.sendEvent("BACK_TO_MENU");
                return;
            }

            listenerService.pushListenerToChat(message.chat(), m -> {
                try {
                    int cellNumber = Integer.parseInt(m.text());
                    if (cellNumber > 19 || cellNumber < 1) {
                        bot.execute(new SendMessage(message.chat().id(),
                                "Вы добрались до края света, дальше живут драконы.\n" + //
                                        "\n" + //
                                        "Напишите правильный номер территории и возвращайтесь.")
                                .replyMarkup(menuKeyboard));
                    } else {
                        String ownerGuildName = cellService
                                .getOwnerGuildNameByNumber(cellNumber);

                        if (ownerGuildName != null) {
                            bot.execute(new SendMessage(message.chat().id(), "Клеткой владеет гильдия: "
                                    + ownerGuildName).replyMarkup(menuKeyboard));
                            sm.sendEvent("BACK_TO_MENU");
                            return;
                        }

                        var orders = cellService.getSumOfOrdersOfGuildByNumber(cellNumber);

                        if (orders.size() == 0) {
                            bot.execute(new SendMessage(message.chat().id(),
                                    "Эта клетка не принадлежит никакой гильдии\n\nИнвестируйте и помогайте своей гильдии захватывать территории.")
                                    .replyMarkup(menuKeyboard));
                            sm.sendEvent("BACK_TO_MENU");
                            return;
                        }

                        StringBuilder answer = new StringBuilder(
                                "Вложения в эту клетку:\n");
                        Set<String> keySet = orders.keySet();
                        for (String key : keySet) {
                            answer.append(key + " : " + orders.get(key) + "\n");
                        }
                        bot.execute(new SendMessage(message.chat().id(), answer.toString()).replyMarkup(menuKeyboard));
                    }
                } catch (NumberFormatException e) {
                    bot.execute(new SendMessage(message.chat().id(),
                            "Вы добрались до края света, дальше живут драконы.\n\nНапишите правильный номер территории и возвращайтесь.")
                            .replyMarkup(menuKeyboard));
                }
                sm.sendEvent("BACK_TO_MENU");
            });

        };
    }
}
