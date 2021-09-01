package com.crypto.moneymachine.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Data
@NoArgsConstructor
@Builder
@ToString
@Table(name = "currency")
public class CurrencyEntity extends BaseEntity {

    @Id
    String symbol;

    public CurrencyEntity(String symbol) {
        this.symbol = symbol;
    }
}
