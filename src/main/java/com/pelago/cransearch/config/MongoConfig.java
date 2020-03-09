package com.pelago.cransearch.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;

import java.util.Arrays;
import java.util.List;

@Configuration
public class MongoConfig {
    private String database;
    private String host;
    private int port;

    public MongoConfig(
        @Value("${spring.data.mongodb.database}") String database,
        @Value("${spring.data.mongodb.host}") String host,
        @Value("${spring.data.mongodb.port}") int port) {
        this.database = database;
        this.host = host;
        this.port = port;
    }

    @Bean
    public MongoTemplate getMongoTemplate() {
        final MongoTemplate template = new MongoTemplate(getMongoClient(), database);
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        return template;
    }

    @Bean
    public MongoClient getMongoClient() {
        return MongoClients.create(getMongoClientSettings());
    }

    private MongoClientSettings getMongoClientSettings() {
        final List<ServerAddress> hosts = Arrays.asList(new ServerAddress(host, port));
        return MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(hosts))
                .build();
    }
}
