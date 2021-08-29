package com.crypto.moneymachine.connection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:connection.properties")
public class ConnectionConfig {

    @Value("${account.apiKey}")
    String apiKey;
    @Value("${account.secret}")
    String secret;

    @Bean
    public ConnectionManager connectionManager() {
        return ConnectionManager.getInstance(apiKey, secret);
    }
}
