package com.github.lhotari.dbcontainer.yugabyte.readrestart;

import com.github.lhotari.dbcontainer.DatabaseContainer;
import com.github.lhotari.dbcontainer.yugabyte.YugaByteDatabaseContainer;
import com.github.lhotari.spring.dbcontainers.YugaByteSpringTestContextInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;

public class CustomYugaByteSpringTestContextInitializer extends YugaByteSpringTestContextInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(CustomYugaByteSpringTestContextInitializer.class);

    @Override
    protected DatabaseContainer createDatabaseContainer(ConfigurableEnvironment environment) {
        YugaByteDatabaseContainer yugaByteDatabaseContainer = new YugaByteDatabaseContainer() {
            @Override
            protected List<String> customizeTserverCommand(List<String> command) {
                List<String> modifiedCommand = new ArrayList<>(command);
                modifiedCommand.add("--ysql_max_read_restart_attempts=128");
                LOG.info("command line: " + modifiedCommand);
                return modifiedCommand;
            }

            @Override
            public String getJdbcUrl() {
                // see preferQueryMode in https://jdbc.postgresql.org/documentation/head/connect.html
                return super.getJdbcUrl() + "?preferQueryMode=" + System.getProperty("readrestartbug.preferQueryMode", "extended");
            }
        };
        return yugaByteDatabaseContainer;
    }
}

