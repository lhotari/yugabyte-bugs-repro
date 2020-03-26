package com.github.lhotari.dbcontainer.yugabyte.readrestart;

import com.github.lhotari.spring.dbcontainers.PostgresSpringTestContextInitializer;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = PostgresSpringTestContextInitializer.class)
public class EmbeddedPostgresReadRestartBugTest extends AbstractReadRestartBugTest {

}
