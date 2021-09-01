package com.crypto.moneymachine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table(name = "currency_pair")
public class CurrencyPairEntity extends BaseEntity {

    @Id
    String symbol;

    @ManyToOne
    @JoinColumn(name = "first")
    CurrencyEntity first;

    @ManyToOne
    @JoinColumn(name = "second")
    CurrencyEntity second;
}
