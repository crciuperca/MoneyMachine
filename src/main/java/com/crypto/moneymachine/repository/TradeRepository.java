package com.crypto.moneymachine.repository;

import com.crypto.moneymachine.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TradeRepository extends JpaRepository<TradeEntity, UUID> {

    @Query("select t from TradeEntity t where t.status = 'NEW' and t.side = 'BUY' and t.currencyPair.symbol = :pair")
    public TradeEntity getNewestOpenTrade(@Param("pair") String pair);
}
