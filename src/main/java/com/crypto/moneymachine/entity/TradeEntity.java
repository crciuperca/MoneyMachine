package com.crypto.moneymachine.entity;

import com.crypto.moneymachine.util.OrderStatus;
import com.crypto.moneymachine.util.TradeDecision;
import com.crypto.moneymachine.util.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table(name = "trade")
public class TradeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @OneToMany
    @JoinColumn(name = "trade_id")
    List<OrderEntity> orders;

    @Column(name = "type")
    String tradeType;

    @ManyToOne
    @JoinColumn(name = "pair")
    CurrencyPairEntity currencyPair;

    @ManyToOne
    @JoinColumn(name = "from_currency")
    CurrencyEntity fromCurrency;

    @ManyToOne
    @JoinColumn(name = "to_currency")
    CurrencyEntity toCurrency;

    Double price;

    @Column(name = "stop_price")
    Double stopPrice;

    @Column(name = "stop_limit")
    Double stopLimit;

    Double quantity;

    @Column(name = "executed_quantity")
    Double executedQuantity;

    Double profit;

    @Column(name = "percent_profit")
    Double percentProfit;

    @Column(name = "order_id")
    String orderId;

    @Column(name = "side")
    String side;

    @Column(name = "status")
    String status;


}
