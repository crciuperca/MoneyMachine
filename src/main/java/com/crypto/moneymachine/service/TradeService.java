package com.crypto.moneymachine.service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.crypto.moneymachine.entity.CurrencyPairEntity;
import com.crypto.moneymachine.entity.TradeEntity;
import com.crypto.moneymachine.pojo.MyCandlestick;
import com.crypto.moneymachine.repository.CurrencyPairRepository;
import com.crypto.moneymachine.repository.CurrencyRepository;
import com.crypto.moneymachine.repository.TradeRepository;
import com.crypto.moneymachine.util.OrderStatus;
import com.crypto.moneymachine.util.TradeDecision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TradeService {

//    CandlestickInterval emaInterval = CandlestickInterval.ONE_MINUTE;
    int emaPeriod = 9;
    int macdPeriod1 = 12;
    int macdPeriod2 = 26;
    int signalPeriod = 9;
    //    Double profitLimit = 1.01d;
//    Double stopLossLimit = 0.995d;
//    Double buyQuantityUSDT = 15d;
    Double tradeTax = 0.00075d;
    //    String pair = "EGLDUSDT";
//    String asset = "EGLD";
    long fifteenHoursMillis = 1000 * 60 * 900;

    @Autowired
    TradeRepository tradeRepository;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    CurrencyPairRepository currencyPairRepository;
    @Autowired
    BalancesService balancesService;

    public String buyCurrencyAtPrice(BinanceApiRestClient client, String pair, String quantity, String price) {
        NewOrder order = new NewOrder(pair, OrderSide.BUY, OrderType.LIMIT, TimeInForce.GTC, quantity, price);
//        NewOrder order = new NewOrder(pair, OrderSide.BUY, OrderType.LIMIT, TimeInForce.GTC, quantity, price);
//        client.newOCO()
        return client.newOrder(order).toString();
    }

    public NewOrderResponse buyCurrencyAtMarketPrice(BinanceApiRestClient client, String pair, String quantity) {
        NewOrder order = new NewOrder(pair, OrderSide.BUY, OrderType.MARKET, TimeInForce.GTC, quantity);
        return client.newOrder(order);
    }

    public NewOrderResponse sellCurrencyAtMarketPrice(BinanceApiRestClient client, String pair, String quantity) {
        NewOrder order = new NewOrder(pair, OrderSide.BUY, OrderType.MARKET, TimeInForce.GTC, quantity);
        return client.newOrder(order);
    }

    public String getOpenOrders(BinanceApiRestClient client, String pair) {
        return client.getOpenOrders(new OrderRequest(pair)).stream().map(o -> o.toString()).collect(Collectors.joining("\n"));
    }

    public void checkIfCanTrade(BinanceApiRestClient client, String pair, String asset, Double stopLossLimit, Double takeProfitLimit, Double buyQuantityUSDT, CandlestickInterval customInterval) {
        List<MyCandlestick> candles = getCandles(client, pair,customInterval);
        TradeEntity openBuyTrade = tradeRepository.getNewestOpenTrade(pair);
        Double currentPrice = Double.valueOf(client.getPrice(pair).getPrice());
        TradeDecision tradeDecision = determineOrderType(candles);
        System.out.println(tradeDecision);
        if (openBuyTrade != null &&
                ((hitProfitLimit(currentPrice, openBuyTrade, takeProfitLimit) || hitStopLossLimit(currentPrice, openBuyTrade, stopLossLimit)) || tradeDecision.equals(TradeDecision.SELL))) {

            System.out.println("B");
            String availableQuantityString = client.getAccount().getBalances().stream().filter(b -> b.getAsset().equals(asset)).findFirst().get().getFree().substring(0, 4);

            double availableQuantity = Double.valueOf(availableQuantityString);
            NewOrderResponse sellOrderResponse = sellCurrencyAtMarketPrice(client, pair, availableQuantityString);
            Trade lastTrade = getLastTrade(client, pair);

            openBuyTrade.setStatus(OrderStatus.CLOSED.toString());
            tradeRepository.save(openBuyTrade);

            Double sellPrice = Double.parseDouble(lastTrade.getPrice());
            Double sellQty = availableQuantity;
            Double sellTotal = Double.parseDouble(lastTrade.getQuoteQty());
            Double tradeTaxLoss = (-1d) * sellTotal * tradeTax;

            Double profit = sellTotal + tradeTaxLoss - openBuyTrade.getQuantity() * openBuyTrade.getPrice() + openBuyTrade.getProfit();
            Double percentProfit = profit / openBuyTrade.getPrice() * 100d;
            TradeEntity sellTrade = TradeEntity.builder()
                    .tradeType(sellOrderResponse.getType().name())
                    .currencyPair(openBuyTrade.getCurrencyPair())
                    .fromCurrency(openBuyTrade.getToCurrency())
                    .toCurrency(openBuyTrade.getFromCurrency())
                    .price(sellPrice)
                    .stopPrice(openBuyTrade.getStopPrice())
                    .stopLimit(openBuyTrade.getStopLimit())
                    .quantity(sellQty)
                    .profit(profit)
                    .percentProfit(percentProfit)
                    .orderId(lastTrade.getId().toString())
                    .side("SELL")
                    .status("CLOSED")
                    .build();
            tradeRepository.save(sellTrade);
            balancesService.persistBalances(client);

            if (tradeDecision.equals(TradeDecision.SELL)) {
                System.out.println("[" + new Date() + "] SELL (" + openBuyTrade.getQuantity() + "*" + openBuyTrade.getPrice() + "[" + openBuyTrade.getExecutedQuantity() + "] -> " + sellTrade.getQuantity() + "*" + sellTrade.getPrice() + "[" + sellTrade.getExecutedQuantity() + "]) profit=" + profit + "(" + percentProfit + "%)");
            } else if (currentPrice > openBuyTrade.getPrice()) {
                System.out.println("[" + new Date() + "] Hit Profit limit (" + openBuyTrade.getQuantity() + "*" + openBuyTrade.getPrice() + "[" + openBuyTrade.getExecutedQuantity() + "] -> " + sellTrade.getQuantity() + "*" + sellTrade.getPrice() + "[" + sellTrade.getExecutedQuantity() + "]) profit=" + profit + "(" + percentProfit + "%)");
            } else {
                System.out.println("[" + new Date() + "] Hit StopLoss limit (" + openBuyTrade.getQuantity() + "*" + openBuyTrade.getPrice() + "[" + openBuyTrade.getExecutedQuantity() + "] -> " + sellTrade.getQuantity() + "*" + sellTrade.getPrice() + "[" + sellTrade.getExecutedQuantity() + "]) profit=" + profit + "(" + percentProfit + "%)");
            }
        } else {

            System.out.println("C");
            if (tradeDecision.equals(TradeDecision.BUY) && openBuyTrade == null) {

                System.out.println("D");
                NewOrderResponse buyOrderResponse = buyCurrencyAtMarketPrice(client, pair, buyQuantityUSDT.toString());
                Trade lastTrade = getLastTrade(client, pair);

                Double buyPrice = Double.parseDouble(lastTrade.getPrice());
                Double buyQuantity = Double.parseDouble(lastTrade.getQty());
                Double buyTotal = Double.parseDouble(lastTrade.getQuoteQty());
                Double profit = (-1d) * tradeTax * buyTotal;
                Double percentProfit = (-1d) * tradeTax * 100d;

                CurrencyPairEntity pairEntity = currencyPairRepository.getById(pair);

                TradeEntity buyTrade = TradeEntity.builder()
                        .tradeType(buyOrderResponse.getType().name())
                        .currencyPair(pairEntity)
                        .fromCurrency(pairEntity.getSecond())
                        .toCurrency(pairEntity.getFirst())
                        .price(buyPrice)
                        .stopPrice(buyPrice * takeProfitLimit)
                        .stopLimit(buyPrice * stopLossLimit)
                        .quantity(buyQuantity)
                        .profit(profit)
                        .percentProfit(percentProfit)
                        .orderId(lastTrade.getId().toString())
                        .side("BUY")
                        .status("NEW")
                        .build();

                tradeRepository.save(buyTrade);
                System.out.println("[" + new Date() + "] BUY (" + openBuyTrade.getQuantity() + "*" + openBuyTrade.getPrice() + ")  profit=" + profit + "(" + percentProfit + "%)");
            }

        }

    }

    public List<MyCandlestick> getCandles(BinanceApiRestClient client, String pair, CandlestickInterval customInterval) {
        return getCandles(client, pair, customInterval, null, null);
    }


    public List<MyCandlestick> getCandles(BinanceApiRestClient client, String customPair, CandlestickInterval customInterval, Long startEpoch, Long endEpoch) {
        List<MyCandlestick> candlesticks = new ArrayList<>();
        if (startEpoch == null || endEpoch == null) {
            candlesticks.addAll(client.getCandlestickBars(customPair, customInterval, 1000, (new Date()).getTime() - fifteenHoursMillis, (new Date()).getTime()).stream().map(c -> new MyCandlestick(c, customInterval)).collect(Collectors.toList()));
            System.out.println("Default size: " + candlesticks.size());
        } else {
            long fifteenHoursMultiple = (endEpoch - startEpoch) / fifteenHoursMillis;
            long fifteenHoursRest = (endEpoch - startEpoch) % fifteenHoursMillis;
            System.out.println("[" + startEpoch + "->" + endEpoch + "] Multiple=" + fifteenHoursMultiple + " Rest=" + fifteenHoursRest);
            if (fifteenHoursMultiple > 0) {
                for (int i = 0; i <= fifteenHoursMultiple; i++) {
                    long start = startEpoch + i * fifteenHoursMillis;
                    long end = start;
                    if (i != fifteenHoursMultiple) {
                        end += (i + 1) * fifteenHoursMillis;
                    } else {
                        end += fifteenHoursRest;
                    }
                    List<MyCandlestick> part = client.getCandlestickBars(customPair, customInterval, 1000, start, end).stream().map(c -> new MyCandlestick(c, customInterval)).collect(Collectors.toList());
                    System.out.println("Part size: " + part.size() + " " + start + " -> " + end);
                    candlesticks.addAll(part);
                }
            } else {
                List<MyCandlestick> part = client.getCandlestickBars(customPair, customInterval, 1000, startEpoch, endEpoch).stream().map(c -> new MyCandlestick(c, customInterval)).collect(Collectors.toList());
                candlesticks.addAll(part);
                System.out.println("Direct size: " + part.size());
            }
        }

        System.out.println("Candlesticks size: " + candlesticks.size());
        System.out.println("Candlesticks size in hours: " + (1d * candlesticks.size() / 60d));
        List<MyCandlestick> emaCandlesticks = calculateEMA(candlesticks, emaPeriod);
        List<MyCandlestick> populatedCandlesticks = calculateMacdAndSignal(emaCandlesticks, macdPeriod1, macdPeriod2, signalPeriod);
        return populatedCandlesticks;
    }

    public List<MyCandlestick> calculateEMA(List<MyCandlestick> candlesticks, int period) { //basedOn macd or normal avg

        List<MyCandlestick> newList = new ArrayList<>();

        Double multiplier = 2d / (period + 1d);
        double avg = 0;
        double prevEMA = 0;

        for (int i = 0; i < candlesticks.size(); i++) {
            MyCandlestick currentCandlestick = candlesticks.get(i);
            double closePrice = currentCandlestick.getClose();
            if (i < period) {
                avg += closePrice / period;
                prevEMA = avg;
            } else {
                double ema = (closePrice - prevEMA) * multiplier + prevEMA;

                prevEMA = ema;
                MyCandlestick newCandlestick = currentCandlestick.clone();
                newCandlestick.setEma(ema);
                newList.add(newCandlestick);
            }
        }
//        System.out.println("EMA " + newList.get(0).getEma());
        return newList;
    }

    public List<MyCandlestick> calculateSignal(List<MyCandlestick> candlesticks, int period) { //basedOn macd or normal avg
        List<MyCandlestick> newList = new ArrayList<>();

        Double multiplier = 2d / (period + 1d);
        double avg = 0;
        double prevEMA = 0;

        for (int i = 0; i < candlesticks.size(); i++) {
            MyCandlestick currentCandlestick = candlesticks.get(i);
            double macd = currentCandlestick.getMacd();
            if (i < period) {
                avg += macd / period;
                prevEMA = avg;
            } else {
                double signal = (macd - prevEMA) * multiplier + prevEMA;
                prevEMA = signal;
                MyCandlestick newCandlestick = currentCandlestick;
                newCandlestick.setSignal(signal);
                newList.add(newCandlestick);
            }
        }
        return newList;
    }

    public List<MyCandlestick> calculateMacdAndSignal(List<MyCandlestick> candlesticksWithEMA, int macdPeriod1, int macdPeriod2, int signalPeriod) {
        if (macdPeriod2 - macdPeriod1 >= candlesticksWithEMA.size() - macdPeriod1) {
            return new ArrayList<>();
        }
        List<MyCandlestick> firstMacdLine = calculateEMA(candlesticksWithEMA, macdPeriod1)
                .subList(macdPeriod2 - macdPeriod1, candlesticksWithEMA.size() - macdPeriod1);

        List<MyCandlestick> secondMacdLine = calculateEMA(candlesticksWithEMA, macdPeriod2);

        List<MyCandlestick> macdLine = new ArrayList<>();
        for (int i = 0; i < firstMacdLine.size(); i++) {
            MyCandlestick firstMacdLineCandlestick = firstMacdLine.get(i);
            MyCandlestick secondMacdLineCandlestick = secondMacdLine.get(i);
            double newMacd = firstMacdLineCandlestick.getEma() - secondMacdLineCandlestick.getEma();
            MyCandlestick newCandlestick = firstMacdLineCandlestick.clone();
            newCandlestick.setMacd(newMacd);

            macdLine.add(newCandlestick);
        }
        List<MyCandlestick> macdAndSignalLine = calculateSignal(macdLine, signalPeriod);

        return macdAndSignalLine;
    }

    public TradeDecision determineOrderType(List<MyCandlestick> candles) {
        TradeDecision decision = null;
        MyCandlestick lastCandle = candles.get(candles.size() - 1);
        MyCandlestick secondToLastCandle = candles.get(candles.size() - 2);
        MyCandlestick thirdToLastCandle = candles.get(candles.size() - 3);

        if (/*lastCandle.getMacd() < 0 && */(lastCandle.getMacd() - lastCandle.getSignal()) >= 0 &&
                (secondToLastCandle.getMacd() - secondToLastCandle.getSignal()) < 0 &&
                (thirdToLastCandle.getMacd() - thirdToLastCandle.getSignal()) < 0) {
            decision = TradeDecision.BUY;
        } else if ((lastCandle.getMacd() - lastCandle.getSignal()) <= 0 &&
                (secondToLastCandle.getMacd() - secondToLastCandle.getSignal()) > 0 &&
                (thirdToLastCandle.getMacd() - thirdToLastCandle.getSignal()) > 0) {

            decision = TradeDecision.SELL;
        } else {
            decision = TradeDecision.WAIT;
        }


        return decision;
    }

    public double backTest(List<MyCandlestick> candles, double customProfitLimit, double customStopLossLimit, double customInitialSum, double customBuySum, boolean show, String currency) {
        boolean withSum = false;
//        double initialUSDTBalance = 1000d;
        double USDTBalance = customInitialSum;
        double EGLDBalance = 0d;
        double buySum = customBuySum;
        boolean openOrder = false;
        double buyPrice = -1d;
        int win = 0;
        int lose = 0;
        double totalPricediffPercent = 0d;
        double totalProfit = 0d;
        double totalTradeTax = 0d;
        for (int i = 2; i < candles.size(); i++) {
            TradeDecision decision = determineOrderType(Arrays.asList(candles.get(i - 2), candles.get(i - 1), candles.get(i)));


            if (show && decision.equals(TradeDecision.BUY)) System.out.println();
//            if (!decision.equals(TradeDecision.WAIT)) System.out.println("[" + decision + "] " + candles.get(i).getOpenTime()/* + " [" + candles.get(i - 2) + " "+ candles.get(i - 1) + " " + candles.get(i) + "]"*/);
            double currentPrice = (candles.get(i).getClose() + candles.get(i).getHigh() + candles.get(i).getClose()) / 3;

            if (openOrder) {
                if (decision.equals(TradeDecision.SELL) || currentPrice >= buyPrice * customProfitLimit || currentPrice <= buyPrice * customStopLossLimit) {
                    double soldFor = EGLDBalance * currentPrice;
                    double sellTax = buySum * tradeTax;
                    totalProfit += (soldFor - buySum);
                    totalTradeTax += sellTax;
                    USDTBalance += soldFor;
                    USDTBalance -= sellTax;
                    if (show)
                        System.out.print("[SELL] [" + candles.get(i).getOpenTime() + "] " + EGLDBalance + " " + currency + " at " + currentPrice + " USDT for " + soldFor + " [" + USDTBalance + " USDT  " + EGLDBalance + " " + currency + "] ");
                    double priceDiffPercent = (currentPrice - buyPrice) / buyPrice * 100;
                    totalPricediffPercent += priceDiffPercent;
                    if (show) System.out.print("[accumulated tax = " + totalTradeTax + " USDT] ");
                    if (show) System.out.print("[profit = " + totalProfit + " USDT]");
                    if (show) System.out.print("[" + priceDiffPercent + "% priceDiff] ");
                    if (priceDiffPercent < 0) {
                        if (show) System.out.println(" BAD\n");
                        win++;
                    } else {
                        lose++;
                        if (show) System.out.println(" GOOD\n");
                    }
                    openOrder = false;
                    EGLDBalance = 0;
                }
            } else {
                if (decision.equals(TradeDecision.BUY)) {
                    double boughtEGLD = buySum / currentPrice;
                    buyPrice = currentPrice;
                    EGLDBalance += boughtEGLD;
                    double buyTax = buySum * tradeTax;
                    totalTradeTax += buyTax;
                    USDTBalance -= buySum;
                    USDTBalance -= buyTax;
                    if (show)
                        System.out.print("[BUY] [" + candles.get(i).getOpenTime() + "] " + boughtEGLD + " "+ currency + " at " + currentPrice + " USDT for " + buySum + " [" + USDTBalance + " USDT  " + EGLDBalance + " "+ currency + "] ");
                    if (show) System.out.println("[accumulated tax = " + totalTradeTax + " USDT]");
                    openOrder = true;
                }
            }

        }
//        System.out.println(win + " WINS  &  " + lose + " LOSSES");
//        System.out.println("Total profit: " + totalProfit + " USDT");
//        System.out.println("Total tax: " + totalTradeTax + " USDT");
//        System.out.println("Net profit: " + (totalProfit - totalTradeTax) + " USDT");
//        System.out.println("Percentage profit: " + ((totalProfit - totalTradeTax) / initialUSDTBalance) * 100 + " USDT");
        double percentProfit = (((totalProfit - totalTradeTax) / customInitialSum) * 100);

        if (show)
            System.out.println("[StopLoss=" + customStopLossLimit + "% ProfitLimit=" + customProfitLimit + "%] [ BUYSUM = " + customBuySum + " ] [ " + win + " WIN  " + lose + " LOSE ] [ " + percentProfit + "% PROFIT ]");
        return percentProfit;
    }

    public boolean hitProfitLimit(Double currentPrice, TradeEntity trade, Double tp) {
        if (trade == null) return false;
        Double targetPrice = tp * trade.getPrice();
        return currentPrice >= targetPrice;
    }

    public boolean hitStopLossLimit(Double currentPrice, TradeEntity trade, Double sl) {
        if (trade == null) return false;
        Double limitPrice = sl * trade.getPrice();
        return currentPrice <= limitPrice;
    }

    public Trade getLastTrade(BinanceApiRestClient client, String pair) {
        List<Trade> myTrades = client.getMyTrades(pair);
        Trade lastTrade = myTrades.get(myTrades.size() - 1);
        return lastTrade;
    }

    public double backTest(BinanceApiRestClient client, Boolean simple, Integer hours,
                         Double customStopLossLimit, Double customTakeProfitLimit,
                         Double initialSum, Double customBuySum,
                         String customPair, String customIntervalString,
                         String currency, Boolean show) {
        double bestPercentProfit = -100d;
        double bestSL = 0d;
        double bestPL = 0d;
        double bestSum = 0d;
        double bestPercentProfitSub50 = -100d;
        double bestSLSub50 = 0d;
        double bestPLSub50 = 0d;
        double bestSumSub50 = 0d;
        double bestPercentProfitSub100 = -100d;
        double bestSLSub100 = 0d;
        double bestPLSub100 = 0d;
        double bestSumSub100 = 0d;
        double bestPercentProfitSub200 = -100d;
        double bestSLSub200 = 0d;
        double bestPLSub200 = 0d;
        double bestSumSub200 = 0d;
        double bestPercentProfitSub300 = -100d;
        double bestSLSub300 = 0d;
        double bestPLSub300 = 0d;
        double bestSumSub300 = 0d;
        double bestPercentProfitSub400 = -100d;
        double bestSLSub400 = 0d;
        double bestPLSub400 = 0d;
        double bestSumSub400 = 0d;

        long weekMillis = 1000 * 60 * 60 * 24 * 7;
        long dayMillis = 1000 * 60 * 60 * 24;
        long hourMillis = 1000 * 60 * 60;
        List<MyCandlestick> candles = getCandles(client, customPair, CandlestickInterval.valueOf(customIntervalString), (new Date()).getTime() - hourMillis * hours, (new Date()).getTime());
//        List<MyCandlestick> candles = tradeService.getCandles(connectionManager.getClient());

        if (simple) {
            return backTest(candles, customTakeProfitLimit, customStopLossLimit, initialSum, customBuySum, true, currency);
        } else {
            for (double sum = 20; sum < customBuySum; sum += 10) {
                for (double cp = 1d; cp <= customTakeProfitLimit; cp += 0.001d) {
                    for (double sl = 1d; sl >= customStopLossLimit; sl -= 0.001d) {
                        double percentProfit = backTest(candles, cp, sl, 1000, sum, false, currency);
                        if (percentProfit > bestPercentProfit) {
                            bestPercentProfit = percentProfit;
                            bestSL = sl;
                            bestPL = cp;
                            bestSum = sum;
                        }
                        if (percentProfit > bestPercentProfitSub50 && sum <= 20) {
                            bestPercentProfitSub50 = percentProfit;
                            bestSLSub50 = sl;
                            bestPLSub50 = cp;
                            bestSumSub50 = sum;
                        }
                        if (percentProfit > bestPercentProfitSub100 && sum <= 30) {
                            bestPercentProfitSub100 = percentProfit;
                            bestSLSub100 = sl;
                            bestPLSub100 = cp;
                            bestSumSub100 = sum;
                        }
                        if (percentProfit > bestPercentProfitSub200 && sum <= 40) {
                            bestPercentProfitSub200 = percentProfit;
                            bestSLSub200 = sl;
                            bestPLSub200 = cp;
                            bestSumSub200 = sum;
                        }
                        if (percentProfit > bestPercentProfitSub300 && sum <= 50) {
                            bestPercentProfitSub300 = percentProfit;
                            bestSLSub300 = sl;
                            bestPLSub300 = cp;
                            bestSumSub300 = sum;
                        }
                        if (percentProfit > bestPercentProfitSub400 && sum <= 100) {
                            bestPercentProfitSub400 = percentProfit;
                            bestSLSub400 = sl;
                            bestPLSub400 = cp;
                            bestSumSub400 = sum;
                        }
                    }
                }
            }
            if (show) {
                System.out.println("[BEST  ALL] [StopLoss=" + bestSL * 100 + "% ProfitLimit=" + bestPL * 100 + "%] [ PROFIT = " + bestPercentProfit + "% ] [ SUM = " + bestSum);
                System.out.println("[BEST  <20] [StopLoss=" + bestSLSub50 * 100 + "% ProfitLimit=" + bestPLSub50 * 100 + "%] [ PROFIT = " + bestPercentProfitSub50 + "% ] [ SUM = " + bestSumSub50);
                System.out.println("[BEST  <30] [StopLoss=" + bestSLSub100 * 100 + "% ProfitLimit=" + bestPLSub100 * 100 + "%] [ PROFIT = " + bestPercentProfitSub100 + "% ] [ SUM = " + bestSumSub100);
                System.out.println("[BEST  <40] [StopLoss=" + bestSLSub200 * 100 + "% ProfitLimit=" + bestPLSub200 * 100 + "%] [ PROFIT = " + bestPercentProfitSub200 + "% ] [ SUM = " + bestSumSub200);
                System.out.println("[BEST  <50] [StopLoss=" + bestSLSub300 * 100 + "% ProfitLimit=" + bestPLSub300 * 100 + "%] [ PROFIT = " + bestPercentProfitSub300 + "% ] [ SUM = " + bestSumSub300);
                System.out.println("[BEST <100] [StopLoss=" + bestSLSub400 * 100 + "% ProfitLimit=" + bestPLSub400 * 100 + "%] [ PROFIT = " + bestPercentProfitSub400 + "% ] [ SUM = " + bestSumSub400);
            }
            return bestPercentProfit;
        }
    }

    public void findBestPair(BinanceApiRestClient client, Boolean simple, Integer hours,
                             Double customStopLossLimit, Double customTakeProfitLimit,
                             Double initialSum, Double customBuySum, String customIntervalString) {
        List<Pair<String, String>> pairList = client.getAllPrices().stream()
                .filter(tp -> tp.getSymbol().endsWith("USDT"))
                .map(tp -> Pair.of(tp.getSymbol(), tp.getSymbol().substring(0, tp.getSymbol().length() - 4))).collect(Collectors.toList());
        System.out.println("Pairs: " + pairList.size());

        Pair<Pair<String, String>, Double> bestPair = Pair.of(Pair.of("", ""), -10d);
        Pair<Pair<String, String>, Double> secondBestPair = null;
        Pair<Pair<String, String>, Double> thirdBestPair = null;
        List<Pair<Pair<String, String>, Double>> results = new ArrayList<>();
        for (Pair<String, String> pair : pairList) {
            double profitPercent = backTest(client, simple, hours, customStopLossLimit, customTakeProfitLimit, initialSum,
                    customBuySum, pair.getFirst(), customIntervalString, pair.getSecond(), false);
            Pair<Pair<String, String>, Double> current = Pair.of(pair, profitPercent);
            results.add(current);
            if (profitPercent > bestPair.getSecond()) {
                thirdBestPair = secondBestPair;
                secondBestPair = bestPair;
                bestPair = current;
            }
        }

        Collections.sort(results, (o1, o2) -> (int)(1000* o1.getSecond() - 1000* o2.getSecond()));
        System.out.println();
        System.out.println("1st place: " + bestPair);
        System.out.println("2nd place: " + secondBestPair);
        System.out.println("3rd place: " + thirdBestPair);
        System.out.println();
        for(int i = 0; i < 10; i++) {
            System.out.println(results.get(i));
        }
    }
}
