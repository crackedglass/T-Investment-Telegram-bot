package com.example.tgbotdemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue
    private long orderId;

    @Column(name = "amount")
    private int amount;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "cell_id", referencedColumnName = "cell_id")
    private Cell cell;

    protected Order() {
    };

    public Order(User user, Cell cell, int amount) {
        this.user = user;
        this.cell = cell;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return String.format("Order[user.username : %s, cell.number : %s, amount : %d]", user.getUsername(),
                cell.getNumber(), amount);
    }

}
