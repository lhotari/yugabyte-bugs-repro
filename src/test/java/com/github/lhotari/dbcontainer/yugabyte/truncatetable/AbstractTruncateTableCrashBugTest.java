package com.github.lhotari.dbcontainer.yugabyte.truncatetable;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcOperations;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reproduces some YugaByte crashes when using TRUNCATE TABLE
 */
@SpringBootTest(classes = AbstractTruncateTableCrashBugTest.TestSpringConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractTruncateTableCrashBugTest {

    private ExecutorService executorService;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestSpringConfiguration {

    }

    @Autowired
    JdbcOperations jdbcOperations;

    @BeforeAll
    void createTable() {
        jdbcOperations.execute("DROP TABLE IF EXISTS my_child");
        jdbcOperations.execute("DROP TABLE IF EXISTS my_table");
        jdbcOperations.execute("CREATE TABLE my_table (id UUID primary key, created timestamp not null)");
        jdbcOperations.execute("CREATE TABLE my_child (id UUID primary key, my_table_id UUID, FOREIGN KEY (my_table_id) REFERENCES my_table (id))");
    }

    @BeforeAll
    void createExecutor() {
        executorService = Executors.newFixedThreadPool(10);
    }

    @AfterAll
    void shutdownExecutor() {
        executorService.shutdown();
    }

    @Test
    void shouldAddRowsAndTruncateTable() {
        AtomicInteger insertCount = new AtomicInteger();
        for (int i = 0; i < 1000; i++) {
            System.out.println("i:" + i);
            for (int j = 0; j < 100; j++) {
                executorService.execute(() -> {
                    System.out.println("insert:" + insertCount.incrementAndGet());
                    try {
                        UUID my_table_id = UUID.randomUUID();
                        jdbcOperations.update("INSERT INTO my_table values (?, ?)", my_table_id, new Date());
                        jdbcOperations.update("INSERT INTO my_child values (?, ?)", UUID.randomUUID(), my_table_id);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                });
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                //
            }
            try {
                jdbcOperations.execute("TRUNCATE TABLE my_table, my_child CASCADE");
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
