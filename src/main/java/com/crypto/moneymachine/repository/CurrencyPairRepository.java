package com.crypto.moneymachine.repository;

import com.crypto.moneymachine.entity.CurrencyPairEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyPairRepository extends JpaRepository<CurrencyPairEntity, String> {
}
