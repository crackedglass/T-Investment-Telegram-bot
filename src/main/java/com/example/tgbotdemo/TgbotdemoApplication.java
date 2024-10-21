package com.example.tgbotdemo;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import com.example.tgbotdemo.domain.*;
import com.example.tgbotdemo.services.AdminService;
import com.example.tgbotdemo.services.CellService;
import com.example.tgbotdemo.services.ChatService;
import com.example.tgbotdemo.services.GuildService;
import com.example.tgbotdemo.services.OrderService;
import com.example.tgbotdemo.services.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
@SpringBootApplication
public class TgbotdemoApplication {
	@Autowired
	private ChatService chatService;
	@Autowired
	private GuildService guildService;
	@Autowired
	private UserService userService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private CellService cellService;
	@Autowired
	private AdminService adminService;

	@Autowired
	private TelegramBot bot;

	public static void main(String[] args) {
		SpringApplication.run(TgbotdemoApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(Environment environment) {
		return args -> {
			Faker faker = new Faker();

			ClassPathResource resource = new ClassPathResource("/jsons/map.json");
			ObjectMapper objectMapper = new ObjectMapper();
			List<Map<String, Object>> map = objectMapper.readValue(resource.getFile(),
					new TypeReference<>() {
					});

			List<Cell> cells = new ArrayList<>();
			for (Map<String, Object> m : map) {
				int number = (int) m.get("number");
				int level = (int) m.get("level");
				List<Object> i = (List<Object>) m.get("neighbours");
				int[] neighbours = i.stream().mapToInt(x -> (int) x).toArray();
				Cell newCell = new Cell(number, level, null, neighbours);
				cellService.save(newCell);
				cells.add(newCell);
			}

			List<Guild> guilds = new ArrayList<>();
			for (int i = 1; i <= 3; i++) {
				guilds.add(new Guild("Guild " + i));
				guildService.save(guilds.getLast());
			}

			List<User> users = new ArrayList<>();

			for (Guild g : guilds) {
				for (int i = 0; i <= 50; i++) {
					User fakeUser = new User(faker.name().username(),
							faker.number().numberBetween(100, 1000), g);
					userService.save(fakeUser);
					users.add(fakeUser);
				}
			}

			for (User u : users) {
				orderService
						.save(new Order(u, cells.get(new Random().nextInt(0, 12)), new Random().nextInt(10, 100)));
			}

			Guild toSave = guilds.getFirst();
			userService.save(new User("mymarichko", 10000, toSave));
			userService.save(new User("ya_qlgn", 523, toSave));
			userService.save(new User("Ereteik", 1000, guilds.getLast()));

			adminService.save(new Admin("ya_qlgn"));
			adminService.save(new Admin("Ereteik"));
			// userService.save(new User("ya_qlgn", 0, null));

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

}
