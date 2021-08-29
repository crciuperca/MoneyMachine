package com.crypto.moneymachine.controller;

import com.crypto.moneymachine.connection.ConnectionManager;
import com.crypto.moneymachine.pojo.CurrentBalance;
import com.crypto.moneymachine.service.BalancesService;
import com.crypto.moneymachine.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trade")
public class TradeController {

    @Autowired
    TradeService tradeService;
    @Autowired
    ConnectionManager connectionManager;

    @GetMapping("/test")
    public String testTrade() {
        return tradeService.buyCurrencyAtPrice(connectionManager.getClient(), "EGLDUSDT", "1", "30");
    }

    @GetMapping("/openOrders/{pair}")
    public String getOpenOrders(@PathVariable String pair) {
        return tradeService.getOpenOrders(connectionManager.getClient(), pair);
    }
//
//    @GetMapping("/main")
//    public String getMainBalances() {
//        return balancesService.showMainBalances(connectionManager.getClient());
//    }
//
//    @GetMapping("/get/{currency}")
//    public String getSpecificBalance(@PathVariable String currency) {
//        return balancesService.showSpecificBalance(connectionManager.getClient(), currency);
//    }
//
//    @GetMapping("/get2/{currency}")
//    public CurrentBalance getSpecificBalance2(@PathVariable String currency) {
//        return balancesService.getSpecificBalance(connectionManager.getClient(), currency);
//    }

}