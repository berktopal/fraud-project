package com.berqbank.fraudengine.repository;

import com.berqbank.fraudengine.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Velocity Kuralı (Hız/Sıklık) için: Belirli bir hesaptan son X dakikada yapılan işlem sayısını bulur
    long countByAccountIdAndTimestampAfter(Long accountId, LocalDateTime timestamp);
}