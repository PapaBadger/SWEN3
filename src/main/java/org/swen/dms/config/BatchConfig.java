package org.swen.dms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class BatchConfig {
    // This class is empty; the annotation triggers the background task runner.
}