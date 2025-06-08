package com.dauducbach.profile_service.configuration;

import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class Neo4jConfig {
    @Bean(name = "reactiveTransactionManager")
    public ReactiveNeo4jTransactionManager reactiveNeo4jTransactionManager(Driver driver) {
        return new ReactiveNeo4jTransactionManager(driver);
    }
}
