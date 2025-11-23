package org.swen.dms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// 1. Tell JPA to ONLY look in the 'jpa' sub-package
@EnableJpaRepositories(basePackages = "org.swen.dms.repository.jpa")

// 2. Tell Elasticsearch to ONLY look in the 'search' sub-package
@EnableElasticsearchRepositories(basePackages = "org.swen.dms.repository.search")
public class DmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DmsApplication.class, args);
    }
}