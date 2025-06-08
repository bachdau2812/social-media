package com.dauducbach.identity_service.configuration;

import com.dauducbach.identity_service.service.IdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdGeneratorConfig {
    @Value("${app.datacenter-id}")
    private long datacenterId;

    @Value("${app.worker-id}")
    private long workerId;

    @Bean
    public IdGenerator idGenerator(){
        return new IdGenerator(datacenterId, workerId);
    }
}
