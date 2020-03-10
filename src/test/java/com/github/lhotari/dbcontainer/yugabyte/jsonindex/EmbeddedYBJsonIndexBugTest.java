package com.github.lhotari.dbcontainer.yugabyte.jsonindex;

import com.github.lhotari.spring.dbcontainers.YugaByteSpringTestContextInitializer;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = YugaByteSpringTestContextInitializer.class)
public class EmbeddedYBJsonIndexBugTest extends AbstractJsonIndexBugTest {

}
