package com.example.tgbotdemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "coalitions")
public class Coalition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coalition_id")
    private Long coalitionId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "first_guild_id", referencedColumnName = "guild_id")
    private Guild firstGuild;

    @ManyToOne(optional = false)
    @JoinColumn(name = "second_guild_id", referencedColumnName = "guild_id")
    private Guild secondGuild;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cell_id", referencedColumnName = "cell_id")
    private Cell cell;

    protected Coalition() {
    }

    public Coalition(Guild firstGuild, Guild secondGuild, Cell cell) {
        this.firstGuild = firstGuild;
        this.secondGuild = secondGuild;
        this.cell = cell;
    }
}
