package com.crypto.moneymachine.controller;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.crypto.moneymachine.connection.ConnectionManager;
import com.crypto.moneymachine.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    ConnectionManager connectionManager;
    @Autowired
    CurrencyRepository currencyRepository;

    @GetMapping("/get/{id}")
    public String testGet(@PathVariable int id) {
        BinanceApiRestClient restClient = connectionManager.getClient();
//        restClient.newOrder(new NewOrder());
        List<AssetBalance> assets = restClient.getAccount().getBalances();
        assets.forEach(a -> {
            if (a.getAsset().equals("BNB") || a.getAsset().equals("EGLD") || a.getAsset().equals("USDT") || a.getAsset().equals("EUR")) {
                System.out.println(a.toString());
            }
        });
        return "You chose " + id;
    }

    @GetMapping("/price/all")
    public String testGet() {
        BinanceApiRestClient restClient = connectionManager.getClient();
        return restClient.getAllPrices().stream().map(p -> p.toString()).collect(Collectors.joining("\n"));
    }

    @GetMapping("/price/get/{pair}")
    public String testGet(@PathVariable String pair) {
        BinanceApiRestClient restClient = connectionManager.getClient();


        return restClient.getPrice(pair).toString();
//        return restClient.getAllPrices().stream().filter(p -> p.getSymbol().equals(pair)).map(p -> p.toString()).findFirst().get();
    }



    @GetMapping("/currency/all")
    public String getAllCurrencies() {



        return currencyRepository.findAll().toString();
    }

}
