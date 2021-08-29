package com.crypto.moneymachine.service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.market.TickerPrice;
import com.crypto.moneymachine.entity.CurrencyEntity;
import com.crypto.moneymachine.pojo.CurrentBalance;
import com.crypto.moneymachine.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BalancesService {

    @Autowired
    CurrencyRepository currencyRepository;

    public BalancesService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public String showAllBalances(BinanceApiRestClient client) {
        List<AssetBalance> assets = client.getAccount().getBalances();
        return assets.stream().map(a -> a.getAsset() + ": " + a.getFree() + " [" + a.getLocked() + "]").collect(Collectors.joining("\n"));
    }

    public String showMainBalances(BinanceApiRestClient client) {
        List<AssetBalance> assets = client.getAccount().getBalances();
        return assets.stream().filter(a -> "EGLD".equals(a.getAsset()) || "USDT".equals(a.getAsset()) || "BNB".equals(a.getAsset()) || "EUR".equals(a.getAsset())).map(a -> a.getAsset() + ": " + a.getFree() + " [" + a.getLocked() + "]").collect(Collectors.joining("\n"));
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

    public void persistBalances(BinanceApiRestClient client) {
        List<AssetBalance> assets = client.getAccount().getBalances();
        List<AssetBalance> nonEmptyBalances = assets.stream().filter(asset -> (Double.parseDouble(asset.getFree()) + Double.parseDouble(asset.getLocked())) > 0).collect(Collectors.toList());
        persistNewCurrencies(nonEmptyBalances);
    }

    public void persistNewCurrencies(List<AssetBalance> nonEmptyBalances) {
        List<String> savedCurrencies = currencyRepository.findAll().stream().map(currencyEntity -> currencyEntity != null ? currencyEntity.getSymbol() : null).filter(symbol -> symbol != null).collect(Collectors.toList());
        List<CurrencyEntity> newCurrencies = nonEmptyBalances.stream().map(assetBalance -> assetBalance.getAsset()).filter(symbol -> !savedCurrencies.contains(symbol)).map(symbol -> new CurrencyEntity(symbol)).collect(Collectors.toList());
        System.out.println("Saving new currencies: " + newCurrencies);
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
