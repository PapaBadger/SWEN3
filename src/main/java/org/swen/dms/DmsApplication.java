package org.swen.dms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the DMS (Document Management System) application.
 * <p>
 * This class boots the Spring context and starts the embedded application
 * server (Tomcat by default). It also triggers component scanning for all
 * packages under {@code org.swen.dms}.
 */

@SpringBootApplication
public class DmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DmsApplication.class, args);
    }

}
