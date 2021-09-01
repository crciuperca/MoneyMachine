package com.crypto.moneymachine.repository;

import com.crypto.moneymachine.entity.BalanceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BalanceHistoryRepository extends JpaRepository<BalanceHistoryEntity, UUID> {
}
