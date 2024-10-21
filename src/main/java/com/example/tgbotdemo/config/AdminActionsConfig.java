package com.example.tgbotdemo.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;

import com.example.tgbotdemo.domain.Cell;
import com.example.tgbotdemo.domain.Guild;
import com.example.tgbotdemo.domain.statemachine.ChatStates;
import com.example.tgbotdemo.services.BlockService;
import com.example.tgbotdemo.services.CellService;
import com.example.tgbotdemo.services.GuildService;
import com.example.tgbotdemo.services.ListenerService;
import com.example.tgbotdemo.services.OrderService;
import com.example.tgbotdemo.utils.ResourceUtil;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Document;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class AdminActionsConfig {

    @Autowired
    private ResourceUtil resourceUtil;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private CellService cellService;

    @Autowired
    private GuildService guildService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TelegramBot bot;

    private Keyboard unblockedKeyboard = new ReplyKeyboardMarkup(new KeyboardButton[][] {
            { new KeyboardButton("Остановить вложения") },
            { new KeyboardButton("Добавить серебро пользователям") },
            { new KeyboardButton("Загрузить таблицу с пользователями") },
            { new KeyboardButton("Загрузить карту") }, // TODO
            { new KeyboardButton("Закрепить клетки за гильдиями") },
            { new KeyboardButton("Выйти в главное меню") }
    });

    Keyboard blockedKeyboard = new ReplyKeyboardMarkup(new KeyboardButton[][] {
            { new KeyboardButton("Возобновить вложения") },
            { new KeyboardButton("Добавить серебро пользователям") },
            { new KeyboardButton("Загрузить таблицу с пользователями") },
            { new KeyboardButton("Загрузить карту") },
            { new KeyboardButton("Закрепить клетки за гильдиями") },
            { new KeyboardButton("Выйти в главное меню") }
    });

    @Bean
    public Action<ChatStates, String> sendAdminMenu() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            Keyboard adminKeyboard = blockService.isBlocked() ? blockedKeyboard : unblockedKeyboard;
            bot.execute(new SendMessage(message.chat().id(), "Вы в админ-меню").replyMarkup(adminKeyboard));
        };
    }

    @Bean
    public Action<ChatStates, String> addMoneyToUsers() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            Keyboard adminKeyboard = blockService.isBlocked() ? blockedKeyboard : unblockedKeyboard;

            bot.execute(new SendMessage(message.chat().id(),
                    "В файле <em>temp.xlsx</em> указаны все зарегистрированные пользователи. Значения введенные в столбце B напротив пользователя будут добавлены к его счёту.")
                    .parseMode(ParseMode.HTML));
            bot.execute(new SendDocument(message.chat().id(), resourceUtil.generateTableOfUsers()));
            bot.execute(new SendMessage(message.chat().id(), "Загрузите файл:").replyMarkup(new ReplyKeyboardRemove()));

            listenerService.pushListenerToChat(message.chat(), m -> {

                if (m.document() == null) {
                    bot.execute(new SendMessage(m.chat().id(), "Файл не найден").replyMarkup(adminKeyboard));
                    return;
                }

                String fileId = m.document().fileId();
                GetFileResponse getFileResponse = bot.execute(new GetFile(fileId));
                String filePath = getFileResponse.file().filePath();

                resourceUtil.processAddMoneyFile(filePath);

                bot.execute(new SendMessage(m.chat().id(), "Серебро добавлено").replyMarkup(adminKeyboard));
            });
        };
    }

    @Bean
    public Action<ChatStates, String> loadUsers() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            Keyboard adminKeyboard = blockService.isBlocked() ? blockedKeyboard : unblockedKeyboard;

            bot.execute(new SendMessage(message.chat().id(),
                    "<b>Как заполнять <em>users.xlsx</em>?</b>\n1. Каждая гильдия на отдельном листе, название листа соотвествует названию гильдии.\n"
                            +
                            "2. В первом столбце заполняются ники пользователей в тг(без @)\n" +
                            "3. Во втором столбце - начальное серебро")
                    .parseMode(ParseMode.HTML));
            bot.execute(new SendDocument(message.chat().id(), resourceUtil.getUsersTemplate()));
            bot.execute(new SendMessage(message.chat().id(), "Загрузите файл:").replyMarkup(new ReplyKeyboardRemove()));

            listenerService.pushListenerToChat(message.chat(), m -> {

                if (m.document() == null) {
                    bot.execute(new SendMessage(m.chat().id(), "Файл не найден").replyMarkup(adminKeyboard));
                    return;
                }

                String fileId = m.document().fileId();
                GetFileResponse getFileResponse = bot.execute(new GetFile(fileId));
                String filePath = getFileResponse.file().filePath();

                resourceUtil.loadUsers(filePath);

                bot.execute(new SendMessage(m.chat().id(), "Пользователи загружены").replyMarkup(adminKeyboard));
            });
        };
    }

    @Bean
    public Action<ChatStates, String> stopTrades() {
        return context -> {
            blockService.setBlocked();
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            Keyboard adminKeyboard = blockService.isBlocked() ? blockedKeyboard : unblockedKeyboard;

            bot.execute(new SendMessage(message.chat().id(),
                    "Сейчас пользователи не могут делать вложения, они станут доступны после выхода из админ-меню или по нажатию на кнопку \"Возобновить вложения\"")
                    .replyMarkup(adminKeyboard));

        };
    }

    @Bean
    public Action<ChatStates, String> restartTrades() {
        return context -> {
            blockService.removeBlocked();
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            Keyboard adminKeyboard = blockService.isBlocked() ? blockedKeyboard : unblockedKeyboard;

            bot.execute(new SendMessage(message.chat().id(),
                    "Вложения возобновлены")
                    .replyMarkup(adminKeyboard));

        };
    }

    @Bean
    public Action<ChatStates, String> fixWinners() {
        return context -> {
            log.info("Fixing cell by winners");
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            List<Cell> cells = cellService.getAllCells();
            StringBuilder changes = new StringBuilder("Новая карта:\n");

            for (Cell cell : cells) {
                int numberOfCell = cell.getNumber();
                var sumsOfGuildsOrders = cellService.getSumOfOrdersOfGuildByNumber(numberOfCell);
                Guild ownerGuild = cell.getOwnerGuild();
                String cellOwner = " ";
                if (ownerGuild != null)
                    cellOwner = ownerGuild.getName();

                if (sumsOfGuildsOrders.size() != 0) {
                    Set<String> keySet = sumsOfGuildsOrders.keySet();
                    String maxGuildName = keySet.stream()
                            .reduce((a, b) -> sumsOfGuildsOrders.get(a) > sumsOfGuildsOrders.get(b) ? a : b).get();
                    Guild winner = guildService.getByName(maxGuildName);
                    cell.setOwnerGuild(winner);
                    cellOwner = maxGuildName;
                }

                changes.append(String.format("Клетка %d - %s\n", cell.getNumber(), cellOwner));
                cellService.save(cell);
            }

            orderService.deleteAll();

            bot.execute(new SendMessage(message.chat().id(), changes.toString()));

        };
    }

    @Bean
    public Action<ChatStates, String> loadNewMap() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");
            Keyboard adminKeyboard = blockService.isBlocked() ? blockedKeyboard : unblockedKeyboard;

            bot.execute(new SendMessage(message.chat().id(),
                    "Загрузите новую карту, как *файл* \n\nИначе карта будет в 144p").parseMode(ParseMode.Markdown)
                    .replyMarkup(new ReplyKeyboardRemove()));
            listenerService.pushListenerToChat(message.chat(), m -> {
                Document photo = m.document();
                if (photo == null) {
                    bot.execute(new SendMessage(m.chat().id(), "Файл не найден").replyMarkup(adminKeyboard));
                    return;
                }

                String fileId = photo.fileId();
                GetFileResponse response = bot.execute(new GetFile(fileId));
                String filePath = response.file().filePath();

                try {
                    resourceUtil.loadNewMap(filePath);
                } catch (Exception e) {
                    bot.execute(new SendMessage(m.chat().id(), "Не удалось загрузить новую карту")
                            .replyMarkup(adminKeyboard));
                    e.printStackTrace();
                }
            });

        };
    }

}
