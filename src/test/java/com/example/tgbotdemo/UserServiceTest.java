package com.example.tgbotdemo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.tgbotdemo.services.UserService;

@SpringBootTest

public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void test() {
        assertEquals("ya_qlgn", userService.findByUsernameWithGuild("YA_QLGN").getUsername());
    }
}
