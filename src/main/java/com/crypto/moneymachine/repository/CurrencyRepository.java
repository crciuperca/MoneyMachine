package com.crypto.moneymachine.repository;

import com.crypto.moneymachine.entity.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

//@Repository
public interface CurrencyRepository extends JpaRepository<CurrencyEntity, UUID> {
}
