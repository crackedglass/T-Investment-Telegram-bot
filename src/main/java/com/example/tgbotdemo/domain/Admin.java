package com.example.tgbotdemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
@Table(name = "admins")
public class Admin {
    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private long userId;
    @Column(name = "username")
    private String username;

    protected Admin() {
    };

    public Admin(String name) {
        this.username = name;
    }
}
