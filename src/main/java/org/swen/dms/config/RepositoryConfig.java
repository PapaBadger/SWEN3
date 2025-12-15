package org.swen.dms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "org.swen.dms.repository.jpa")
@EnableElasticsearchRepositories(basePackages = "org.swen.dms.repository.search")
public class RepositoryConfig {
    // This class handles the specific routing of repositories.
    // By keeping it separate, @WebMvcTest won't load it!
}