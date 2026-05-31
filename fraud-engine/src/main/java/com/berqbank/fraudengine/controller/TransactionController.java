package com.berqbank.fraudengine.controller;

import com.berqbank.fraudengine.entity.Transaction;
import com.berqbank.fraudengine.entity.TransactionStatus;
import com.berqbank.fraudengine.repository.AccountRepository;
import com.berqbank.fraudengine.repository.TransactionRepository;
import com.berqbank.fraudengine.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final FraudDetectionService fraudDetectionService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @PostMapping("/process")
    public String processTransaction(@RequestBody TransactionRequest req) {
        var account = accountRepository.findByAccountNumber(req.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı"));

        var transaction = Transaction.builder()
                .account(account)
                .targetAccountNumber(req.getTargetAccountNumber())
                .amount(req.getAmount())
                .ipAddress(req.getIpAddress())
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();

        var calculatedStatus = fraudDetectionService.evaluateTransaction(transaction, account);
        transaction.setStatus(calculatedStatus);
        
        transactionRepository.save(transaction);
        return "İşlem sonucu: " + calculatedStatus;
    }

    @GetMapping
    public List<Transaction> getAll() { 
        return transactionRepository.findAll(); 
    }

    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestParam TransactionStatus status) {
        var tx = transactionRepository.findById(id).orElseThrow();
        tx.setStatus(status);
        tx.setApprovedBy("Müfettiş_Berk");
        tx.setProcessedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }
}