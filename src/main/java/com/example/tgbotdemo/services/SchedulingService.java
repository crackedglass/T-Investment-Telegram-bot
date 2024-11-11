package com.example.tgbotdemo.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tgbotdemo.domain.Cell;
import com.example.tgbotdemo.domain.Guild;
import com.example.tgbotdemo.domain.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SchedulingService {

    @Autowired
    private CellService cellService;
    @Autowired
    private GuildService guildService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;

    // @Scheduled(cron = "@hourly")
    // @Transactional
    // public void addMoneyToLeaders() {
    // List<Cell> cells = cellService.getAllCells();
    // for (Cell cell : cells) {
    // var orders = cellService.getSumOfOrdersOfGuildByNumber(cell.getNumber());
    // if (orders.size() == 0)
    // continue;
    // String leaderGuildName = Collections.max(orders.entrySet(),
    // Map.Entry.comparingByValue()).getKey();
    // Guild leaderGuild = guildService.getByName(leaderGuildName);
    // Set<User> users = leaderGuild.getUsers();
    // for (User user : users) {
    // user.setMoney(user.getMoney() + 200);
    // log.info("Added 200 to users of guild: " + leaderGuild.getName());
    // userService.save(user);
    // }
    // }
    // }
}
