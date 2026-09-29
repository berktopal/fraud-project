package com.berqbank.fraudengine.service;

import com.berqbank.fraudengine.entity.Account;
import com.berqbank.fraudengine.entity.Transaction;
import com.berqbank.fraudengine.entity.TransactionStatus;
import com.berqbank.fraudengine.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {
    private final TransactionRepository transactionRepository;
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("50000.00");
    private static final int MAX_TRANSACTIONS_PER_MINUTE = 3;

    public TransactionStatus evaluateTransaction(Transaction transaction, Account account) {
        // Savunma amaçlı: geçersiz tutar asla onaylanmaz
        if (transaction.getAmount() == null || transaction.getAmount().signum() <= 0) return TransactionStatus.BLOCKED;
        if (transaction.getAmount().compareTo(MAX_TRANSACTION_AMOUNT) > 0) return TransactionStatus.FLAGGED;

        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        long count = transactionRepository.countByAccountIdAndTimestampAfter(account.getId(), oneMinuteAgo);
        
        if (count >= MAX_TRANSACTIONS_PER_MINUTE) return TransactionStatus.BLOCKED;

        int riskScore = account.getRiskScore() == null ? 0 : account.getRiskScore();
        if (riskScore > 80 && transaction.getAmount().compareTo(new BigDecimal("10000.00")) > 0) {
            return TransactionStatus.FLAGGED;
        }

        return TransactionStatus.APPROVED;
    }
}