package com.github.lhotari.dbcontainer.yugabyte.readrestart;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = CustomYugaByteSpringTestContextInitializer.class)
public class EmbeddedYBReadRestartBugTest extends AbstractReadRestartBugTest {

}
