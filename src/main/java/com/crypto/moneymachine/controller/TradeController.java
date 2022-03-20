package com.crypto.moneymachine.controller;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.crypto.moneymachine.connection.ConnectionManager;
import com.crypto.moneymachine.pojo.CurrentBalance;
import com.crypto.moneymachine.pojo.MyCandlestick;
import com.crypto.moneymachine.service.BalancesService;
import com.crypto.moneymachine.service.TradeService;
import com.crypto.moneymachine.util.AvgQueue;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/trade")
@AllArgsConstructor
@NoArgsConstructor
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

//    @Scheduled(fixedDelay = 1000)
//    public void checkIfCanTrade() {
//        tradeService.checkIfCanTrade(connectionManager.getClient());
//    }

    @GetMapping("/backtest")
    public void backTest(@RequestParam Boolean simple, @RequestParam Integer hours,
                         @RequestParam Double customStopLossLimit, @RequestParam Double customTakeProfitLimit,
                         @RequestParam Double initialSum, @RequestParam Double customBuySum,
                         @RequestParam String customPair, @RequestParam String customIntervalString,
                         @RequestParam String currency, @RequestParam Boolean debug) {
        tradeService.backTest(connectionManager.getClient(), simple, hours, customStopLossLimit, customTakeProfitLimit,
                initialSum, customBuySum, customPair, customIntervalString, currency, true, debug);
    }

    @GetMapping("/findBestPair")
    public void findBestPair(@RequestParam Boolean simple, @RequestParam Integer hours,
                         @RequestParam Double customStopLossLimit, @RequestParam Double customTakeProfitLimit,
                         @RequestParam Double initialSum, @RequestParam Double customBuySum,
                         @RequestParam String customIntervalString, @RequestParam Boolean debug) {
        tradeService.findBestPair(connectionManager.getClient(), simple, hours, customStopLossLimit, customTakeProfitLimit,
                initialSum, customBuySum, customIntervalString, debug);
    }


    @GetMapping("/buyMaxAtMarketPriceAndSell")
    public void buyAndSell(@RequestParam String pair) {
        tradeService.buyMaxCurrencyAtMarketPriceAndSellAtProfit(connectionManager.getClient(), pair, 0.005d);
    }

    @GetMapping("/sellMaxAtMarketPrice")
    public void cancelAllAndSellAtMarketPrice(@RequestParam String asset, @RequestParam String pair) {
        tradeService.cancelAllAndSellAtMarketPrice(connectionManager.getClient(), pair, asset);
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