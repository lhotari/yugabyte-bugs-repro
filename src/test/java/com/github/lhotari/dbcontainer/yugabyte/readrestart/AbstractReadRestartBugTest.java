package com.github.lhotari.dbcontainer.yugabyte.readrestart;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = AbstractReadRestartBugTest.TestSpringConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractReadRestartBugTest {

    private ExecutorService executorService;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestSpringConfiguration {

    }

    @Autowired
    JdbcOperations jdbcOperations;

    @BeforeAll
    void createTable() throws InterruptedException {
        // YB isn't always initialized, system.transactions object is missing.
        // wait 10 seconds as a workaround
        Thread.sleep(10000L);
        jdbcOperations.execute("DROP TABLE IF EXISTS my_child");
        jdbcOperations.execute("DROP TABLE IF EXISTS my_table");
        jdbcOperations.execute("CREATE TABLE my_table (id UUID primary key, created timestamp not null)");
        jdbcOperations.execute("CREATE TABLE my_child (id UUID primary key, my_table_id UUID, created timestamp not null, FOREIGN KEY (my_table_id) REFERENCES my_table (id))");
    }

    @BeforeAll
    void createExecutor() {
        executorService = Executors.newFixedThreadPool(2);
    }

    @AfterAll
    void shutdownExecutor() {
        executorService.shutdown();
    }

    @Autowired
    PlatformTransactionManager transactionManager;

    AtomicReference<UUID> activeParentId = new AtomicReference<>();

    @Test
    void shouldInsertRowsAndSelectRowsWithoutReadRestartErrors() throws InterruptedException {
        CountDownLatch completionLatch = new CountDownLatch(1);
        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicBoolean terminated = new AtomicBoolean(false);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
        executorService.execute(() -> {
            try {
                for (int i = 0; i < 30; i++) {
                    if (terminated.get()) {
                        break;
                    }
                    try {
                        UUID my_table_id = UUID.randomUUID();
                        System.out.println("i:" + i + " my_table_id:" + my_table_id);
                        transactionTemplate.execute(status -> {
                            jdbcOperations.update("INSERT INTO my_table values (?, ?)", my_table_id, new Date());
                            return null;
                        });
                        for (int j = 0; j < 100; j++) {
                            transactionTemplate.execute(status -> {
                                System.out.print(".");
                                jdbcOperations.update("INSERT INTO my_child values (?, ?, ?)", UUID.randomUUID(), my_table_id, new Date());
                                return null;
                            });
                            activeParentId.set(my_table_id);
                        }
                        System.out.println();
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                        terminated.set(true);
                    }
                }
            } finally {
                completionLatch.countDown();
                completed.set(true);
            }
        });
        executorService.execute(() -> {
            while (!completed.get()) {
                try {
                    UUID my_table_id = activeParentId.get();
                    if (my_table_id != null) {
                        System.out.println("querying " + my_table_id);
                        transactionTemplate.execute(status -> {
                            List<Map<String, Object>> results = jdbcOperations.queryForList("select * from my_child where my_table_id = ?", my_table_id);
                            if (results.isEmpty()) {
                                throw new IllegalStateException("There should have been results for " + my_table_id);
                            }
                            System.out.println("ok " + my_table_id);
                            return null;
                        });
                    } else {
                        Thread.sleep(100L);
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    terminated.set(true);
                }
            }
        });
        assertThat(completionLatch.await(30, TimeUnit.SECONDS)).isTrue();
        assertThat(terminated).as("there were exceptions").isFalse();
    }
}
