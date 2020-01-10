package com.github.lhotari.dbcontainer.yugabyte.truncatetable;

import com.github.lhotari.spring.dbcontainers.PostgresSpringTestContextInitializer;
import org.springframework.test.context.ContextConfiguration;

/**
 * <p>
 * Runs the AbstractTruncateTableCrashBugTest with embedded Postgres to demonstrate that Postgres
 * runs the same test case without issues
 * </p>
 */
@ContextConfiguration(initializers = PostgresSpringTestContextInitializer.class)
public class EmbeddedPostgresTruncateTableCrashBugTest extends AbstractTruncateTableCrashBugTest {

}
