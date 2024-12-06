package com.example.tgbotdemo.services;

import java.util.*;
import org.springframework.stereotype.Service;

import com.example.tgbotdemo.repositories.*;
import com.example.tgbotdemo.domain.*;

@Service
public class CellService {

    private CellRepository cellRepository;
    private UserService userService;
    private GuildService guildService;

    public CellService(CellRepository cellRepository, UserService userService, GuildService guildService) {
        this.cellRepository = cellRepository;
        this.userService = userService;
        this.guildService = guildService;
    }

    public List<Cell> getAllCells() {
        return cellRepository.findAll();
    }

    public Cell getByNumber(int number) {
        return cellRepository.findByNumber(number);
    }

    public List<Integer> getAvailableCellsNumbersByUsername(String username) {
        User user = userService.getByUsername(username);
        if (user == null)
            return List.of();
        if (user.getGuild() == null)
            return List.of();
        Guild userGuild = guildService.getByName(user.getGuild().getName());

        List<Cell> firstLevel = cellRepository.findByLevel(1);
        List<Cell> guildCells = userGuild.getCells().stream().toList();
        Set<Integer> available = new HashSet<>();
        firstLevel.stream().forEach(item -> available.add(item.getNumber()));
        guildCells.stream().forEach(item -> available.add(item.getNumber()));
        for (Cell c : guildCells) {
            int[] neighbours = c.getNeighbours();
            for (int i : neighbours)
                available.add(i);
        }
        return available.stream().toList();
    }

    public Map<String, Integer> getSumOfOrdersOfGuildByNumber(int number) {
        Map<String, Integer> toReturn = new HashMap<>();

        Cell cell = cellRepository.findByNumber(number);
        List<Order> orders = cell.getOrders();
        for (Order o : orders) {
            Guild guild = o.getUser().getGuild();
            if (guild == null)
                continue;

            String guildName = guild.getName();
            if (toReturn.get(guildName) == null)
                toReturn.put(guildName, o.getAmount());
            else {
                int old = toReturn.get(guildName);
                old += o.getAmount();
                toReturn.put(guildName, old);
            }
        }

        return toReturn;
    };

    public String getOwnerGuildNameByNumber(int number) {
        Cell cell = cellRepository.findByNumber(number);
        Guild ownerGuild = cell.getOwnerGuild();

        if (ownerGuild == null)
            return null;
        else
            return ownerGuild.getName();
    }

    public void save(Cell cell) {
        cellRepository.save(cell);
    }

    public void removeAllOwners() {
        List<Cell> cells = cellRepository.findAll();
        for (Cell cell : cells)
            cell.setOwnerGuild(null);
        cellRepository.saveAll(cells);
    }

    public void deleteAll() {
        cellRepository.deleteAll();
    }

}
