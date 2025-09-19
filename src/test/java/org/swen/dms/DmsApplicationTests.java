package org.swen.dms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test for verifying that the Spring context loads successfully.
 * <p>
 * Runs with {@link org.springframework.boot.test.context.SpringBootTest}
 * and the "test" profile (H2 in-memory DB).
 */

@ActiveProfiles("test")
@SpringBootTest
class DmsApplicationTests {

    /** Verifies that the application context can start without errors. */
    @Test
    void contextLoads() {}
}
