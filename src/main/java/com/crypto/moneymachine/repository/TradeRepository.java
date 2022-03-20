package com.crypto.moneymachine.repository;

import com.crypto.moneymachine.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, UUID> {

    @Query("select t from TradeEntity t where t.status = 'NEW' and t.side = 'BUY' and t.currencyPair.symbol = :pair")
    public TradeEntity getNewestOpenTrade(@Param("pair") String pair);
}
