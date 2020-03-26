package com.github.lhotari.dbcontainer.yugabyte.readrestart;

import com.github.lhotari.spring.dbcontainers.YugaByteSpringTestContextInitializer;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = YugaByteSpringTestContextInitializer.class)
public class EmbeddedYBReadRestartBugTest extends AbstractReadRestartBugTest {

}
