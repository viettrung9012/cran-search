package com.pelago.cransearch.config;

import com.mongodb.ServerAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MongoConfigTest {
    public static final String HOST = "localhost";
    public static final int PORT = 27017;
    public static final String DATABASE = "test";

    private MongoConfig config;

    @BeforeEach
    public void setUp() {
        config = new MongoConfig(DATABASE, HOST, PORT);
    }

    @Test
    public void getMongoTemplate() {
        assertEquals(DATABASE, config.getMongoTemplate().getDb().getName());
    }

    @Test
    public void getMongoClient() {
        final List<ServerAddress> hosts = config.getMongoClient().getClusterDescription().getClusterSettings().getHosts();
        assertEquals(1, hosts.size());
        assertEquals(HOST, hosts.get(0).getHost());
        assertEquals(PORT, hosts.get(0).getPort());
    }
}