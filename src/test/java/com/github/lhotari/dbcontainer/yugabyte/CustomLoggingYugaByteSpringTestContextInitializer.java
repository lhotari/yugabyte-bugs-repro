package com.github.lhotari.dbcontainer.yugabyte;

import com.github.lhotari.spring.dbcontainers.LoggingYugaByteSpringTestContextInitializer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * places YugaByte logs and coredumps under the build directory
 */
public class CustomLoggingYugaByteSpringTestContextInitializer extends LoggingYugaByteSpringTestContextInitializer {
    @Override
    protected Path resolveLogsPath() {
        // configure parent directory for logs
        File parentDir = new File("build/yb_logs_and_core");
        parentDir.mkdirs();
        try {
            return Files.createTempDirectory(parentDir.toPath(), "yblogs");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
