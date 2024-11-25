package com.example.tgbotdemo.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private CellService cellService;
    private GuildService guildService;
    private UserService userService;

    public SchedulingService(CellService cs, GuildService gs, UserService us) {
        cellService = cs;
        guildService = gs;
        userService = us;
    }

    @Scheduled(cron = "0 0 12-23,0 * * *", zone = "Europe/Moscow")
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
                        log.info("Added 100 to user:" + user.getUsername() + "of guild: " + leaderGuild.getName());
                        userService.save(user);
                    }
                } else {
                    log.info(String.format("Guild %s not found", leaderGuildName));
                }
            }
        }
    }
}
