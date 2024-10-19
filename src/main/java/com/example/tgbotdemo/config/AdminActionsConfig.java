package com.example.tgbotdemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;

import com.example.tgbotdemo.domain.statemachine.ChatStates;
import com.example.tgbotdemo.services.ListenerService;
import com.example.tgbotdemo.utils.ExcelUtil;
import com.pengrad.telegrambot.TelegramBot;
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

@Configuration
public class AdminActionsConfig {

    @Autowired
    private ExcelUtil excelUtil;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private TelegramBot bot;

    private Keyboard adminKeyboard = new ReplyKeyboardMarkup(
            new KeyboardButton[][] {
                    { new KeyboardButton("Добавить серебро пользователям") },
                    { new KeyboardButton("Загрузить таблицу с пользователями") },
                    { new KeyboardButton("Загрузить карту") },
                    { new KeyboardButton("Закрепить клетки за гильдиями") },
                    { new KeyboardButton("Применить изменения") },
                    { new KeyboardButton("Выйти в главное меню") }
            });

    @Bean
    public Action<ChatStates, String> sendAdminMenu() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");
            bot.execute(new SendMessage(message.chat().id(), "Вы в админ-меню").replyMarkup(adminKeyboard));
        };
    }

    @Bean
    public Action<ChatStates, String> addMoneyToUsers() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            bot.execute(new SendMessage(message.chat().id(),
                    "В файле <em>temp.xlsx</em> указаны все зарегистрированные пользователи. Значения введенные в столбце B напротив пользователя будут добавлены к его счёту.")
                    .parseMode(ParseMode.HTML));
            bot.execute(new SendDocument(message.chat().id(), excelUtil.generateTableOfUsers()));
            bot.execute(new SendMessage(message.chat().id(), "Загрузите файл:").replyMarkup(new ReplyKeyboardRemove()));

            listenerService.pushListenerToChat(message.chat(), m -> {

                if (m.document() == null) {
                    bot.execute(new SendMessage(m.chat().id(), "Файл не найден").replyMarkup(adminKeyboard));
                    return;
                }

                String fileId = m.document().fileId();
                GetFileResponse getFileResponse = bot.execute(new GetFile(fileId));
                String filePath = getFileResponse.file().filePath();

                excelUtil.processAddMoneyFile(filePath);

                bot.execute(new SendMessage(m.chat().id(), "Серебро добавлено").replyMarkup(adminKeyboard));
            });
        };
    }

    @Bean
    public Action<ChatStates, String> loadUsers() {
        return context -> {
            Message message = (Message) context.getExtendedState().getVariables().get("msg");

            bot.execute(new SendMessage(message.chat().id(),
                    "<b>Как заполнять <em>users.xlsx</em>?</b>\n1. Каждая гильдия на отдельном листе, название листа соотвествует названию гильдии.\n"
                            +
                            "2. В первом столбце заполняются ники пользователей в тг(без @)\n" +
                            "3. Во втором столбце - начальное серебро")
                    .parseMode(ParseMode.HTML));
            bot.execute(new SendDocument(message.chat().id(), excelUtil.getUsersTemplate()));
            bot.execute(new SendMessage(message.chat().id(), "Загрузите файл:").replyMarkup(new ReplyKeyboardRemove()));

            listenerService.pushListenerToChat(message.chat(), m -> {

                if (m.document() == null) {
                    bot.execute(new SendMessage(m.chat().id(), "Файл не найден").replyMarkup(adminKeyboard));
                    return;
                }

                String fileId = m.document().fileId();
                GetFileResponse getFileResponse = bot.execute(new GetFile(fileId));
                String filePath = getFileResponse.file().filePath();

                excelUtil.loadUsers(filePath);

                bot.execute(new SendMessage(m.chat().id(), "Пользователи загружены").replyMarkup(adminKeyboard));
            });
        };
    }
}
