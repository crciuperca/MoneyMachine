package com.crypto.moneymachine.controller;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.crypto.moneymachine.connection.ConnectionManager;
import com.crypto.moneymachine.pojo.MyCandlestick;
import com.crypto.moneymachine.repository.CurrencyRepository;
import com.crypto.moneymachine.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    ConnectionManager connectionManager;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    TradeRepository tradeRepository;

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
    @GetMapping("/getCandlesticks")
    public String getCandlesticks() {

        long fifteenHoursMillis = 1000 * 60 * 900;
        return connectionManager.getClient().getCandlestickBars("EGLDUSDT", CandlestickInterval.ONE_MINUTE, 9000, (new Date()).getTime() - fifteenHoursMillis, (new Date()).getTime()).stream().map(p -> p.toString()).collect(Collectors.joining("\n"));
    }

    @GetMapping("/getTrade")
    public String getTrade() {


        return tradeRepository.getNewestOpenTrade("EGLDUSDT").getPrice().toString();
    }
    @GetMapping("/getLastTrade")
    public String getLastTrade() {
        BinanceApiRestClient restClient = connectionManager.getClient();
        List<Trade> myTrades = restClient.getMyTrades("EGLDUSDT");
        Trade lastTrade = myTrades.get(myTrades.size() - 1);
        return lastTrade.toString();
    }

//    @GetMapping("/candles/all")
//    public String getCandles() {
//        BinanceApiRestClient restClient = connectionManager.getClient();
//        CandlestickInterval emaInterval = CandlestickInterval.ONE_MINUTE;
//        int emaPeriod = 9;
//        int macdPeriod1 = 12;
//        int macdPeriod2 = 26;
//        int signalPeriod = 9;
//        List<MyCandlestick> candlesticks = restClient.getCandlestickBars("EGLDUSDT", emaInterval).stream().map(c -> new MyCandlestick(c, emaInterval)).collect(Collectors.toList());
//
//        List<MyCandlestick> emaCandlesticks = calculateEMA(candlesticks, emaPeriod);
//        List<MyCandlestick> populatedCandlesticks = calculateMacdAndSignal(emaCandlesticks, macdPeriod1, macdPeriod2, signalPeriod);
//        return populatedCandlesticks.stream().map(x -> "[" + x.getOpenTime() + "] ClosePrice: " + x.getClose() + " EMA: " + x.getEma() + " MACD: " + x.getMacd() + " SIGNAL: " + x.getSignal()).collect(Collectors.joining("\n"));
//    }



}
