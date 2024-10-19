package com.example.tgbotdemo.domain;

import java.util.List;
import java.util.stream.IntStream;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cells")
public class Cell {
    @Id
    @GeneratedValue
    @Column(name = "cell_id")
    private long cellId;

    @Column(name = "number", nullable = false, unique = true)
    private int number;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "guild_id", referencedColumnName = "guild_id", nullable = true)
    private Guild ownerGuild;

    @Column(name = "level")
    private int level;

    @Column(name = "neighbours")
    private int[] neighbours;

    @OneToMany(mappedBy = "cell")
    private List<Order> orders;

    protected Cell() {
    };

    public Cell(int number, int level, Guild guild, int[] neighbours) {
        this.number = number;
        this.level = level;
        this.ownerGuild = guild;
        this.neighbours = neighbours;
    }

    @Override
    public String toString() {
        return String.format("Cell[number : %d, level: %d, guild: %s, neighbours: %s]", number, level,
                getOwnerGuild().getName(),
                String.join(",", IntStream.of(neighbours).boxed().map(String::valueOf).toList()));
    }

}
