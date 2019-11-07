package com.github.lhotari.dbcontainer.yugabyte.truncatetable;

import com.github.lhotari.dbcontainer.yugabyte.CustomLoggingYugaByteSpringTestContextInitializer;
import org.springframework.test.context.ContextConfiguration;

/**
 * <p>
 * Runs the AbstractTruncateTableCrashBugTest with embedded YugaByte master and tserver containers
 * running in Docker containers controlled by dbcontainers / TestContainers.
 * Logs and core dumps are stored under build/yb_logs_and_core.
 * </p>
 * <p>
 * The assumption is that you run this on a Linux host and configure the coredump pattern this way:
 * <pre>
 * echo '/cores/core.%e.%p' | sudo tee /proc/sys/kernel/core_pattern
 * </pre>
 * Without this configuration, you won't get the coredumps from the containers to the directory mounted from a
 * directory within build/yb_logs_and_core.
 * </p>
 * <p>
 * On MacOSX / Windows, you might be able to change the core_pattern config with this command:
 * <pre>
 * docker run --privileged --rm -it busybox sh -c "echo /cores/core.%e.%p > /proc/sys/kernel/core_pattern"
 * </pre>
 * </p>
 */
@ContextConfiguration(initializers = CustomLoggingYugaByteSpringTestContextInitializer.class)
public class EmbeddedYBTruncateTableCrashBugTest extends AbstractTruncateTableCrashBugTest {

}
