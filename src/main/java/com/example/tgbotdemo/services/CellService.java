package com.example.tgbotdemo.services;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.tgbotdemo.repositories.*;
import com.example.tgbotdemo.domain.*;

@Service
public class CellService {
    @Autowired
    private CellRepository cellRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private GuildRepository guildRepository;

    public List<Cell> getAllCells() {
        return cellRepository.findAll();
    }

    public Cell getByNumber(int number) {
        return cellRepository.findByNumber(number);
    }

    public List<Integer> getAvailableCellsNumbersByUsername(String username) {
        User user = userService.getByUsername(username);
        Guild userGuild = guildRepository
                .findByName(user.getGuild().getName());
        List<Cell> firstLevel = cellRepository.getAvailableCellsForLevel(1);
        List<Cell> guildCells = userGuild.getCells().stream().toList();
        Set<Integer> available = new HashSet<>();
        firstLevel.stream().forEach(item -> available.add(item.getNumber()));
        for (Cell c : guildCells) {
            int[] neighbours = c.getNeighbours();
            for (int i : neighbours)
                available.add(cellRepository.findByNumber(i).getNumber());
        }
        return available.stream().toList();
    }

    public Map<String, Integer> getSumOfOrdersOfGuildByNumber(int number) {
        Map<String, Integer> toReturn = new HashMap<>();

        Cell cell = cellRepository.findByNumber(number);
        List<Order> orders = cell.getOrders();
        for (Order o : orders) {
            String guildName = o.getUser().getGuild().getName();
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
