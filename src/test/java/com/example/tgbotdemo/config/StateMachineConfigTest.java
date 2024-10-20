package com.example.tgbotdemo.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import com.example.tgbotdemo.domain.statemachine.ChatStates;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Slf4j
@SpringBootTest
public class StateMachineConfigTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void testResourceLoader() throws IOException {
        URI uri = resourceLoader.getResource("classpath:").getURI();
        log.info(uri.toString());
        File file = new File(uri + "temp.txt");
        file.createNewFile();
    }
}
