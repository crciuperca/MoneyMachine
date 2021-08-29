package com.crypto.moneymachine.service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.request.OrderRequest;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class TradeService {

    public String buyCurrencyAtPrice(BinanceApiRestClient client, String pair, String quantity, String price) {
        NewOrder order = new NewOrder(pair, OrderSide.BUY, OrderType.LIMIT, TimeInForce.GTC, quantity, price);
//        NewOrder order = new NewOrder(pair, OrderSide.BUY, OrderType.LIMIT, TimeInForce.GTC, quantity, price);
//        client.newOCO()
        return client.newOrder(order).toString();
    }

    public String getOpenOrders(BinanceApiRestClient client, String pair) {
        return client.getOpenOrders(new OrderRequest(pair)).stream().map(o -> o.toString()).collect(Collectors.joining("\n"));
    }
}
