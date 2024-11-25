package com.example.tgbotdemo.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
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

    private CellService cellService;
    private GuildService guildService;
    private UserService userService;
    private OrderService orderService;
    private ListenerService listenerService;
    private TelegramBot bot;

    private Keyboard menuKeyboard = new ReplyKeyboardMarkup(
            new KeyboardButton[][] {
                    { new KeyboardButton("Сколько у меня серебра") },
                    { new KeyboardButton("Сколько серебра у моей гильдии") },
                    { new KeyboardButton("Инвестировать в территорию") },
                    { new KeyboardButton("Мои инвестиции в территории") },
                    { new KeyboardButton("Сколько гильдии инвестировали в территории") },
                    { new KeyboardButton("Обзор карты") }
            });

    public MainActionsConfig(CellService cellService, GuildService guildService, UserService guildService,
            OrderService orderService, ListenerService listenerService, TelegramBot bot){
        this.cellService = cellService;
        this.guildService = guildService;
        this.userService = userService;
        this.orderService = orderService;
        this.listenerService = listenerService;
        this.bot = bot;
    }

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
            log.info("Declined for id: " + message.chat().id() + " username:" + message.chat().username());
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

            if (Optional.ofNullable(user).isPresent()) {

                Guild guild = guildService.getByName(user.getGuild().getName());

                if (guild == null) {
                    log.error("Guild for user: " + message.chat().username() + " not found");
                    bot.execute(new SendMessage(message.chat().id(),
                            "Ваша гильдия не найдена. Обратитесь за помощью к счетоводу."));
                    return;
                }

                List<String> toDisplay = guild
                        .getUsers()
                        .stream()
                        .sorted((a, b) -> a.getMoney() < b.getMoney() ? 1 : -1)
                        .map(y -> " • " + y.getUsername() + " - " + String.valueOf(y.getMoney()))
                        .limit(10)
                        .toList();

                if (toDisplay.size() == 0) {
                    bot.execute(new SendMessage(message.chat().id(), "Вы один в гильдии"));
                    return;
                }

                int sum = guild.getUsers().stream().mapToInt(u -> u.getMoney()).sum();

                String text = String.format("Твоя гильдия заработала " + sum + " серебра\nТоп %d твоих соратников:\n",
                        toDisplay.size())
                        + String.join("\n", toDisplay);

                bot.execute(new SendMessage(chatId, text));
            } else {
                log.error("User " + message.chat().username() + " not found");
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
            } else {
                log.error("User " + message.chat().username() + " not found");
                bot.execute(new SendMessage(chatId, "Вы не найдены в базе данных"));
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

                    if (available.size() == 0) {
                        bot.execute(new SendMessage(m.chat().id(), "У вас нет гильдии").replyMarkup(menuKeyboard));
                        sm.sendEvent("BACK_TO_MENU");
                        log.error("Available cells for user: " + m.chat().username() + " not found");
                        return;
                    }

                    if (available.contains(cellNumber)) {
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
                    if (amount > 0 && amount <= user.getMoney()) {
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

    @Bean
    public Action<ChatStates, String> getOverwiew() {
        return context -> {

            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            List<Cell> cells = cellService.getAllCells();
            cells.sort((a, b) -> (a.getNumber() > b.getNumber()) ? 1 : -1);

            Guild userGuild = userService.findByUsernameWithGuild(message.chat().username()).getGuild();

            StringBuilder list = new StringBuilder("Статистика по клеткам:\n");

            for (Cell cell : cells) {
                int numberOfCell = cell.getNumber();
                var sumsOfGuildsOrders = cellService.getSumOfOrdersOfGuildByNumber(numberOfCell);

                if (sumsOfGuildsOrders.size() != 0) {
                    Entry<String, Integer> max = Collections.max(sumsOfGuildsOrders.entrySet(),
                            Map.Entry.comparingByValue());

                    list.append(
                            String.format("Клетка %d:\n    Лидер: %s - %d\n", numberOfCell, max.getKey(),
                                    max.getValue()));

                    if (userGuild != null) {
                        if (!max.getKey().equals(userGuild.getName())) {
                            Integer sum = sumsOfGuildsOrders.get(userGuild.getName());
                            if (sum != null)
                                list.append(String.format("    Ваша гильдия: %d\n", sum));
                        }
                    }

                } else {
                    list.append(String.format("Клетка %d\n    Нет вложений\n", numberOfCell));
                }
            }
            bot.execute(new SendMessage(message.chat().id(), list.toString()));
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

                        var orders = cellService.getSumOfOrdersOfGuildByNumber(cellNumber);

                        if (orders.size() == 0 && ownerGuildName == null) {
                            bot.execute(new SendMessage(message.chat().id(),
                                    "Эта клетка не принадлежит никакой гильдии\n\nИнвестируйте и помогайте своей гильдии захватывать территории.")
                                    .replyMarkup(menuKeyboard));
                            sm.sendEvent("BACK_TO_MENU");
                            return;
                        }

                        StringBuilder answer = new StringBuilder();
                        if (ownerGuildName != null) {
                            answer.append(String.format("Клеткой владеет гильдия: %s\n", ownerGuildName));
                        }

                        List<String> keys = orders.keySet().stream().collect(Collectors.toList());

                        if (keys.size() == 0) {
                            answer.append("Вложений нет\n");
                        } else {
                            answer.append("Вложения в эту клетку:\n");
                            keys.sort((a, b) -> (orders.get(a) > orders.get(b)) ? -1 : 1);
                            for (String key : keys) {
                                answer.append("    " + key + " : " + orders.get(key) + "\n");
                            }
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
