package com.example.tgbotdemo;

import java.util.Optional;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import com.example.tgbotdemo.services.ChatService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.*;

@Slf4j
@SpringBootApplication
public class TgbotdemoApplication {

	private ChatService chatService;
	private TelegramBot bot;

	public TgbotdemoApplication(ChatService chatService, TelegramBot bot) {
		this.chatService = chatService;
		this.bot = bot;
	}

	public static void main(String[] args) {
		SpringApplication.run(TgbotdemoApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(Environment environment) {
		return args -> {
			bot.setUpdatesListener(updates -> {
				for (Update update : updates) {
					Optional<Message> message = Optional.ofNullable(update.message());
					message.ifPresent(m -> {

						chatService.handleMessage(m);
					});
				}
				return UpdatesListener.CONFIRMED_UPDATES_ALL;
			},
					e -> {
						if (e.response() != null) {
							// got bad response from telegram
							e.response().errorCode();
							e.response().description();
						} else {
							// probably network error
							e.printStackTrace();
						}
					});
			log.info("Bot created with token " + bot.getToken());
		};
	};

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
		log.info(TimeZone.getDefault().toString());
		log.info(LocalTime.now().toString());
	}

}
