package com.github.lhotari.dbcontainer.yugabyte.truncatetable;

import org.springframework.test.context.TestPropertySource;

// runs the AbstractTruncateTableCrashBugTest with provided database
// configure the database connectivity settings here:
@TestPropertySource({
        "spring.datasource.url=jdbc:postgresql://localhost:5433/postgres",
        "spring.datasource.username=postgres",
        "spring.datasource.password=",
        "spring.r2dbc.url=r2dbc:postgresql://localhost:5433/postgres",
        "spring.r2dbc.username=postgres",
        "spring.r2dbc.password="
})
public class ProvidedYBTruncateTableCrashBugTest extends AbstractTruncateTableCrashBugTest {

}
