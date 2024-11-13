package com.example.tgbotdemo.services;

import java.util.Collections;
import java.util.HashMap;
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

    @Scheduled(cron = "0 0 12-23,0 * * *")
    @Transactional
    public void addMoneyToLeaders() {
        List<Guild> guilds = guildService.findAll();
        HashMap<String, Integer> leadershipCount = new HashMap<>();
        guilds.stream().forEach(g -> leadershipCount.put(g.getName(), 0));

        List<Cell> cells = cellService.getAllCells();
        for (Cell cell : cells) {
            var orders = cellService.getSumOfOrdersOfGuildByNumber(cell.getNumber());
            if (orders.size() == 0)
                continue;
            String leaderGuildName = Collections.max(orders.entrySet(),
                    Map.Entry.comparingByValue()).getKey();

            Integer newLeadershipCountForGuild = leadershipCount.get(leaderGuildName) + 1;
            leadershipCount.put(leaderGuildName, newLeadershipCountForGuild);

            if (newLeadershipCountForGuild <= 3) {
                Guild leaderGuild = guilds.stream().filter(x -> x.getName().equals(leaderGuildName)).findAny()
                        .orElse(null);

                if (leaderGuild != null) {
                    Set<User> users = leaderGuild.getUsers();
                    for (User user : users) {
                        user.setMoney(user.getMoney() + 100);
                        log.info("Added 100 to users of guild: " + leaderGuild.getName());
                        userService.save(user);
                    }
                } else {
                    log.info(String.format("Guild %s not found", leaderGuildName));
                }
            }
        }
    }
}
