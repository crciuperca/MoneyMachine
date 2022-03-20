package com.crypto.moneymachine.service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.market.TickerPrice;
import com.crypto.moneymachine.entity.BalanceHistoryEntity;
import com.crypto.moneymachine.entity.CurrencyEntity;
import com.crypto.moneymachine.pojo.CurrentBalance;
import com.crypto.moneymachine.repository.BalanceHistoryRepository;
import com.crypto.moneymachine.repository.CurrencyRepository;
import com.crypto.moneymachine.util.Currency;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class BalancesService {

    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    BalanceHistoryRepository balanceHistoryRepository;

    public BalancesService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public String showAllBalances(BinanceApiRestClient client) {
        List<AssetBalance> assets = client.getAccount().getBalances();
        return assets.stream().map(a -> a.getAsset() + ": " + a.getFree() + " [" + a.getLocked() + "]").collect(Collectors.joining("\n"));
    }

    public Map<String, String> showMainBalances(BinanceApiRestClient client) {
        List<AssetBalance> assets = client.getAccount().getBalances();

        Map<String, String> wallet = new HashMap<>();
        assets.stream()
                .filter(a -> "EGLD".equals(a.getAsset()) || "USDT".equals(a.getAsset()) || "BNB".equals(a.getAsset()) || "EUR".equals(a.getAsset()))
                .filter(a -> !("0.00000000".equals(a.getFree()) && "0.00000000".equals(a.getLocked())))
                .forEach(a -> wallet.put(a.getAsset(), String.format("%s [%s]", a.getFree(), a.getLocked())));
        return wallet;
    }

    public String showSpecificBalance(BinanceApiRestClient client, String currency) {
        List<AssetBalance> assets = client.getAccount().getBalances();
        return assets.stream().filter(a -> currency.equals(a.getAsset())).map(a -> a.getAsset() + ": " + a.getFree() + " [" + a.getLocked() + "]").collect(Collectors.joining("\n"));
    }

    public CurrentBalance getSpecificBalance(BinanceApiRestClient client, String currency) {
        List<AssetBalance> assets = client.getAccount().getBalances();
        return assets.stream().filter(a -> currency.equals(a.getAsset())).map(a -> new CurrentBalance(currency, Double.parseDouble(a.getFree()), Double.parseDouble(a.getLocked()))).findFirst().get();
    }
    public Double getBalanceInEuros(BinanceApiRestClient client) {
        List<AssetBalance> assets = client.getAccount().getBalances();
        List<TickerPrice> allPrices = client.getAllPrices();
        Double totalUSDT = assets.stream().map(asset -> {
            Double balance = (Double.parseDouble(asset.getFree()) + Double.parseDouble(asset.getLocked()));
            if (balance == 0) {
                return 0d;
            }
            if (asset.getAsset().equals("USDT")) {
                System.out.println("USDT: " + balance);
                return balance;
            }
            String pair = asset.getAsset() + "USDT";
            TickerPrice emptyTicker = new TickerPrice();
            emptyTicker.setPrice("0");
            Double currencyPriceInUSDT = Double.parseDouble(allPrices.stream().filter(price -> price.getSymbol().equals(pair)).findFirst().orElse(emptyTicker).getPrice());
            if (currencyPriceInUSDT == null) currencyPriceInUSDT = 0d;
            Double currentValue = balance * currencyPriceInUSDT;
            if (currentValue > 0) {
                System.out.println(pair + " balance: " + balance + " priceInUSDT: " + currencyPriceInUSDT + " currVal: " + currentValue);
            }
            return currentValue;
        }).reduce(0d, Double::sum);
        System.out.println("Total USDT: " + totalUSDT);
        Double eurUSDTPrice =  Double.parseDouble(allPrices.stream().filter(price -> price.getSymbol().equals("EURUSDT")).findFirst().get().getPrice());

        return totalUSDT/eurUSDTPrice;
    }

    public List<BalanceHistoryEntity> calculateAllBalances(BinanceApiRestClient client) {
        return calculateAllBalances(client, client.getAccount().getBalances().stream()
                .filter(asset -> (Double.parseDouble(asset.getFree()) + Double.parseDouble(asset.getLocked())) > 0)
                .collect(Collectors.toList()));
    }

    public List<BalanceHistoryEntity> calculateAllBalances(BinanceApiRestClient client, List<AssetBalance> nonEmptyAssets) {
        List<TickerPrice> allPrices = client.getAllPrices();
        List<BalanceHistoryEntity> nonEmptyBalances = nonEmptyAssets.stream()
                .filter(asset -> (Double.parseDouble(asset.getFree()) + Double.parseDouble(asset.getLocked())) > 0)
                .map(asset -> new BalanceHistoryEntity(currencyRepository.findById(asset.getAsset()).get(), (Double.parseDouble(asset.getFree()) + Double.parseDouble(asset.getLocked()))))
                .collect(Collectors.toList());

        Double eurUSDTPrice =  Double.parseDouble(allPrices.stream().filter(price -> price.getSymbol().equals("EURUSDT")).findFirst().get().getPrice());

        Double totalUSDT = nonEmptyBalances.stream().map(balance -> {
            if ("USDT".equals(balance.getCurrency().getSymbol())) {
                System.out.println(balance.getAmount() + "USDT");
                return balance.getAmount();
            }
            String pair = balance.getCurrency().getSymbol() + "USDT";
            TickerPrice emptyTicker = new TickerPrice();
            emptyTicker.setPrice("0");
            Double currencyPriceInUSDT = Double.parseDouble(allPrices.stream().filter(price -> price.getSymbol().equals(pair)).findFirst().orElse(emptyTicker).getPrice());
            System.out.println(balance.getCurrency() + "" + balance.getAmount() + " -> " + (balance.getAmount() * currencyPriceInUSDT) + "USDT");
            return balance.getAmount() * currencyPriceInUSDT;
        }).reduce(0d, Double::sum);

        nonEmptyBalances.add(new BalanceHistoryEntity(currencyRepository.findById(Currency.EUR_EQUIVALENT.name()).get(), totalUSDT / eurUSDTPrice));
        return nonEmptyBalances;
    }

    public void persistBalances(BinanceApiRestClient client) {
        List<AssetBalance> assets = client.getAccount().getBalances();
        List<AssetBalance> nonEmptyBalances = assets.stream().filter(asset -> (Double.parseDouble(asset.getFree()) + Double.parseDouble(asset.getLocked())) > 0).collect(Collectors.toList());
        if (!nonEmptyBalances.isEmpty()) {
            persistNewCurrencies(nonEmptyBalances);
            balanceHistoryRepository.saveAll(calculateAllBalances(client, nonEmptyBalances));
        }

    }

    public void persistNewCurrencies(List<AssetBalance> nonEmptyBalances) {
        List<String> savedCurrencies = currencyRepository.findAll().stream().map(currencyEntity -> currencyEntity != null ? currencyEntity.getSymbol() : null).filter(symbol -> symbol != null).collect(Collectors.toList());
        List<CurrencyEntity> newCurrencies = nonEmptyBalances.stream().map(assetBalance -> assetBalance.getAsset()).filter(symbol -> !savedCurrencies.contains(symbol)).map(symbol -> new CurrencyEntity(symbol)).collect(Collectors.toList());
//        System.out.println("Saving new currencies: " + newCurrencies);
        currencyRepository.saveAll(newCurrencies);
    }

//    public String getMainBalances(int id) {
//        List<AssetBalance> assets = restClient.getAccount().getBalances();
//        assets.forEach(a -> {
//            if (a.getAsset().equals("BNB") || a.getAsset().equals("EGLD") || a.getAsset().equals("USDT") || a.getAsset().equals("EUR")) {
//                System.out.println(a.toString());
//            }
//        });
//        return "You chose " + id;
//    }
//
//    public String getSpecificBalance(String pair) {
//        List<AssetBalance> assets = restClient.getAccount().getBalances();
//        assets.forEach(a -> {
//            if (a.getAsset().equals("BNB") || a.getAsset().equals("EGLD") || a.getAsset().equals("USDT") || a.getAsset().equals("EUR")) {
//                System.out.println(a.toString());
//            }
//        });
//        return "You chose " + id;
//    }
}
