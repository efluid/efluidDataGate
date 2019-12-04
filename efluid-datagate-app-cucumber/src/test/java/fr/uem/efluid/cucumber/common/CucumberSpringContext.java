package fr.uem.efluid.cucumber.common;

import io.cucumber.java.Before;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Definition of the spring related configuration for all cucumber tests. Use a specific model
 * defined for cucumber, with specific method annotated <tt>Before</tt>
 * "setup_cucumber_spring_context" used before each spring context launch
 *
 * <p>For details on the why and the how of this class, see in cucumber jvm-spring integration code
 * ...
 */
@SpringBootTest
@ContextConfiguration(classes = {SystemTestConfig.class})
@AutoConfigureMockMvc
public class CucumberSpringContext {
    // Spring cfg entry point

    @Before
    public void setup_cucumber_spring_context() {
        // Dummy method so cucumber will recognize this class as glue
        // and use its context configuration.
    }
}
