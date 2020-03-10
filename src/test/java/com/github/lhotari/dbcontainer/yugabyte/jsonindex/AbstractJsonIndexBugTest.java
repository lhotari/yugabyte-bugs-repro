package com.github.lhotari.dbcontainer.yugabyte.jsonindex;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.*;

/**
 * Reproduces YugaByte bug with JSON indexes
 */
@SpringBootTest(classes = AbstractJsonIndexBugTest.TestSpringConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractJsonIndexBugTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestSpringConfiguration {

    }

    @Autowired
    JdbcOperations jdbcOperations;

    @Autowired
    PlatformTransactionManager transactionManager;

    @BeforeAll
    void createTable() throws InterruptedException {
        // YB isn't always initialized, system.transactions object is missing.
        // wait 10 seconds as a workaround
        Thread.sleep(10000L);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.execute(status -> {
            jdbcOperations.execute("DROP TABLE IF EXISTS books");
            jdbcOperations.execute("CREATE TABLE books ( id int PRIMARY KEY, details jsonb )");
            // Having an index triggers the json bug in 2.1.1.0-b2. commenting out the following line makes the test pass
            jdbcOperations.execute("CREATE INDEX books_author_first_name ON books ((details->'author'->>'first_name'))");
            return null;
        });
        transactionTemplate.execute(status -> {
            jdbcOperations.execute("INSERT INTO books (id, details) VALUES (1, '{ \"name\": \"Macbeth\", \"author\": { \"first_name\": \"William\", \"last_name\": \"Shakespeare\" }, \"year\": 1623, \"editors\": [\"John\", \"Elizabeth\", \"Jeff\"] }')");
            jdbcOperations.execute("INSERT INTO books (id, details) VALUES (2, '{ \"name\": \"Hamlet\", \"author\": { \"first_name\": \"William\", \"last_name\": \"Shakespeare\" }, \"year\": 1603, \"editors\": [\"Lysa\", \"Mark\", \"Robert\"] }')");
            jdbcOperations.execute("INSERT INTO books (id, details) VALUES (3, '{ \"name\": \"Oliver Twist\", \"author\": { \"first_name\": \"Charles\", \"last_name\": \"Dickens\" }, \"year\": 1838, \"genre\": \"novel\", \"editors\": [\"Mark\", \"Tony\", \"Britney\"] }')");
            jdbcOperations.execute("INSERT INTO books (id, details) VALUES (4, '{ \"name\": \"Great Expectations\", \"author\": { \"first_name\": \"Charles\", \"last_name\": \"Dickens\" }, \"year\": 1950, \"genre\": \"novel\", \"editors\": [\"Robert\", \"John\", \"Melisa\"] }')");
            jdbcOperations.execute("INSERT INTO books (id, details) VALUES (5, '{ \"name\": \"A Brief History of Time\", \"author\": { \"first_name\": \"Stephen\", \"last_name\": \"Hawking\" }, \"year\": 1988, \"genre\": \"science\", \"editors\": [\"Melisa\", \"Mark\", \"John\"] }')");
            return null;
        });
    }

    @Test
    void shouldFindBooksForShakespeareByUsingJsonCondition() {
        assertThat(jdbcOperations
                .queryForList("SELECT id from books where details->'author'->>'first_name'='William'", Integer.class))
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void shouldNotReturnAnyResultsForConditionThatDoesntMatch() {
        // this shouldn't return any results, but it does
        assertThat(jdbcOperations
                .queryForList("SELECT id from books where details->'author'->>'first_name'='Hello World'", Integer.class))
                .isEmpty();
    }
}
