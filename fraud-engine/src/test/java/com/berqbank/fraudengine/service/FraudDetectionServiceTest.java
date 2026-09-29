package com.berqbank.fraudengine.service;

import com.berqbank.fraudengine.entity.Account;
import com.berqbank.fraudengine.entity.Transaction;
import com.berqbank.fraudengine.entity.TransactionStatus;
import com.berqbank.fraudengine.repository.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FraudDetectionServiceTest {

    private final TransactionRepository repo = mock(TransactionRepository.class);
    private final FraudDetectionService service = new FraudDetectionService(repo);

    private TransactionStatus evaluate(String amount, Integer riskScore, long recentCount) {
        when(repo.countByAccountIdAndTimestampAfter(anyLong(), any())).thenReturn(recentCount);
        Account account = Account.builder().id(1L).riskScore(riskScore).build();
        Transaction tx = Transaction.builder().amount(new BigDecimal(amount)).build();
        return service.evaluateTransaction(tx, account);
    }

    @Test
    void negativeOrZeroAmountIsNeverApproved() {
        assertEquals(TransactionStatus.BLOCKED, evaluate("-99999", 10, 0));
        assertEquals(TransactionStatus.BLOCKED, evaluate("0", 10, 0));
    }

    @Test
    void normalTransactionIsApproved() {
        assertEquals(TransactionStatus.APPROVED, evaluate("100", 10, 0));
    }

    @Test
    void largeAmountIsFlagged() {
        assertEquals(TransactionStatus.FLAGGED, evaluate("50000.01", 10, 0));
    }

    @Test
    void velocityLimitBlocks() {
        assertEquals(TransactionStatus.BLOCKED, evaluate("100", 10, 3));
    }

    @Test
    void nullRiskScoreDoesNotCrash() {
        assertEquals(TransactionStatus.APPROVED, evaluate("20000", null, 0));
        assertEquals(TransactionStatus.FLAGGED, evaluate("20000", 90, 0));
    }
}
