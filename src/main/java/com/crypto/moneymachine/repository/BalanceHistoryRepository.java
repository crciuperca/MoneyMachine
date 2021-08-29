package com.crypto.moneymachine.repository;

import com.crypto.moneymachine.entity.BalanceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BalanceHistoryRepository extends JpaRepository<BalanceHistoryEntity, UUID> {
}
