package com.crypto.moneymachine.controller;

import com.crypto.moneymachine.connection.ConnectionManager;
import com.crypto.moneymachine.entity.BalanceHistoryEntity;
import com.crypto.moneymachine.pojo.CurrentBalance;
import com.crypto.moneymachine.service.BalancesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/balances")
@EnableScheduling
public class BalancesController {

    @Autowired
    BalancesService balancesService;
    @Autowired
    ConnectionManager connectionManager;

    @GetMapping("/all")
    public String getAllBalances() {
        return balancesService.showAllBalances(connectionManager.getClient());
    }

    @GetMapping("/main")
    public String getMainBalances() {
        return balancesService.showMainBalances(connectionManager.getClient());
    }

    @GetMapping("/get/{currency}")
    public String getSpecificBalance(@PathVariable String currency) {
        return balancesService.showSpecificBalance(connectionManager.getClient(), currency);
    }

    @GetMapping("/get2/{currency}")
    public CurrentBalance getSpecificBalance2(@PathVariable String currency) {
        return balancesService.getSpecificBalance(connectionManager.getClient(), currency);
    }

    @GetMapping("/equivalent")
    public Double getEquivalentInEUR() {
        return balancesService.getBalanceInEuros(connectionManager.getClient());
    }

    @GetMapping("/allBalances")
    public List<BalanceHistoryEntity> getAllBalanceHistory() {
        return balancesService.calculateAllBalances(connectionManager.getClient());
    }

    //    @Scheduled(cron = "0 * * * * *")
    @GetMapping("/persist")
    public void persistBalanceInEUR() {
        balancesService.persistBalances(connectionManager.getClient());
    }

}
