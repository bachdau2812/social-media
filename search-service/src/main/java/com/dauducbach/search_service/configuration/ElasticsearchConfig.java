package com.dauducbach.search_service.configuration;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

@Configuration
public class ElasticsearchConfig {
    @Bean
    RestClient restClient(){
        return RestClient.builder(new HttpHost("localhost", 9200)).build();
    }

    @Bean
    ReactiveElasticsearchClient reactiveElasticsearchClient(RestClient restClient) {
        return new ReactiveElasticsearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper()));
    }

    @Bean
    public ElasticsearchConverter elasticsearchConverter() {
        return new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext());
    }

    @Bean
    public ReactiveElasticsearchOperations reactiveElasticsearchOperations(
            ReactiveElasticsearchClient client) {
        return new ReactiveElasticsearchTemplate(client, elasticsearchConverter());
    }

}