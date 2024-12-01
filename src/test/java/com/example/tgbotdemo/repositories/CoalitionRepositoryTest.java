package com.example.tgbotdemo.repositories;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.tgbotdemo.domain.Cell;
import com.example.tgbotdemo.domain.Coalition;
import com.example.tgbotdemo.domain.Guild;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Testcontainers
@SpringBootTest
@ContextConfiguration(initializers = CoalitionRepositoryTest.Initializer.class)
public class CoalitionRepositoryTest {

        @Container
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-bookworm");

        @Autowired
        private GuildRepository guildRepository;
        @Autowired
        private CellRepository cellRepository;
        @Autowired
        private CoalitionRepository coalitionRepository;

        @Test
        public void testAssert() {
                assertThat("hello").isEqualTo("hello");
        }

        @Sql("/delete_data.sql")
        @Test
        public void findAll() {
                List<Guild> guilds = List.of(
                                new Guild("test 1"),
                                new Guild("test 2"),
                                new Guild("test 3"));
                guildRepository.saveAll(guilds);

                List<Cell> cells = List.of(
                                new Cell(1, 1, null, null),
                                new Cell(2, 1, null, null));
                cellRepository.saveAll(cells);

                List<Coalition> coalitions = List.of(
                                new Coalition(guilds.get(0), guilds.get(1), cells.get(0)),
                                new Coalition(guilds.get(1), guilds.get(2), cells.get(1)));

                coalitionRepository.saveAll(coalitions);

                var newCoalitions = coalitionRepository.findAll();

                assertThat(newCoalitions.stream().map(Coalition::getCoalitionId).toList())
                                .usingRecursiveAssertion()
                                .isEqualTo(coalitions.stream().map(Coalition::getCoalitionId).toList());
        }

        static class Initializer
                        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
                public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
                        TestPropertyValues.of(
                                        "spring.datasource.url=" + postgres.getJdbcUrl(),
                                        "spring.datasource.username=" + postgres.getUsername(),
                                        "spring.datasource.password=" + postgres.getPassword())
                                        .applyTo(configurableApplicationContext.getEnvironment());
                }
        }
}
