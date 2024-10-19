package com.example.tgbotdemo.domain;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "guilds")
public class Guild {
    @Id
    @GeneratedValue
    @Column(name = "guild_id")
    private long guildId;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "guild")
    private Set<User> users;

    @OneToMany(mappedBy = "ownerGuild")
    private Set<Cell> cells;

    protected Guild() {
    };

    public Guild(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Guild[name : %s, users: %s, cells : %s]", name, users.toString(), cells.toString());
    }

}
