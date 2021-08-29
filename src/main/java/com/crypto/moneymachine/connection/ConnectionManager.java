package com.crypto.moneymachine.connection;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:connection.properties")
public class ConnectionManager {
    private static ConnectionManager instance;
    private static BinanceApiClientFactory clientFactory;
    private static BinanceApiRestClient client;

    private ConnectionManager(String apiKey, String secret) {
        clientFactory = BinanceApiClientFactory.newInstance(apiKey, secret);
        client = clientFactory.newRestClient();
    }

    public static ConnectionManager getInstance(String apiKey, String secret) {
        if (client == null) {
            synchronized (ConnectionManager.class) {
                if (client == null) {
                    instance = new ConnectionManager(apiKey, secret);
                }
            }
        }
        return instance;
    }

    public BinanceApiRestClient getClient() {
        return client;
    }
}
