package com.example.tgbotdemo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tgbotdemo.repositories.*;

import lombok.extern.slf4j.Slf4j;

import com.example.tgbotdemo.domain.*;

@Slf4j
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void create(User user, Cell cell, int amount) {
        log.info("Create transaction on user \"" + user.getUsername() + "\" called Cell: " + cell.getNumber()
                + " amount: " + amount);
        Order exist = orderRepository.findByUserAndCell(user, cell);

        if (exist == null) {
            user.setMoney(user.getMoney() - amount);
            orderRepository.save(new Order(user,
                    cell,
                    amount));
        } else {
            user.setMoney(user.getMoney() + exist.getAmount() - amount);
            if (amount == 0) {
                orderRepository.delete(exist);
            } else {
                exist.setAmount(amount);
                orderRepository.save(exist);
            }

        }

        userRepository.save(user);
    }

    public List<Order> getOrdersByUsername(String username) {
        return orderRepository.findByUsername(username);
    }

    @Transactional
    public void revertAll(List<Order> orders) {
        for (Order order : orders) {
            User user = userRepository.findByUsername(order.getUser().getUsername());
            int money = user.getMoney();
            user.setMoney(money + order.getAmount());
            userRepository.save(user);
            orderRepository.delete(order);
        }
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public void deleteAll() {
        orderRepository.deleteAll();
    }

    public void save(Order order) {
        orderRepository.save(order);
    }
}
