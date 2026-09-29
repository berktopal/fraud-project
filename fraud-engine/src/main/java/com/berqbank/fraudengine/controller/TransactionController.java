package com.berqbank.fraudengine.controller;

import com.berqbank.fraudengine.entity.Transaction;
import com.berqbank.fraudengine.entity.TransactionStatus;
import com.berqbank.fraudengine.repository.AccountRepository;
import com.berqbank.fraudengine.repository.TransactionRepository;
import com.berqbank.fraudengine.service.FraudDetectionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
    public String processTransaction(@RequestBody TransactionRequest req, HttpServletRequest httpRequest) {
        // Negatif/sıfır tutar fraud kurallarını atlatıp APPROVED alabiliyordu
        if (req.getAmount() == null || req.getAmount().signum() <= 0
                || req.getAmount().stripTrailingZeros().scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutar sıfırdan büyük ve en fazla 2 ondalık olmalıdır.");
        }
        if (req.getTargetAccountNumber() == null || req.getTargetAccountNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Alıcı hesap zorunludur.");
        }
        var account = accountRepository.findByAccountNumber(req.getAccountNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hesap bulunamadı"));

        var transaction = Transaction.builder()
                .account(account)
                .targetAccountNumber(req.getTargetAccountNumber())
                .amount(req.getAmount())
                // IP istemcinin gönderdiği alandan değil, bağlantının kendisinden alınır
                // (aksi halde saldırgan IP'yi istediği gibi yazabilirdi)
                .ipAddress(httpRequest.getRemoteAddr())
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
        var tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İşlem bulunamadı"));
        tx.setStatus(status);
        tx.setApprovedBy("Müfettiş_Berk");
        tx.setProcessedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }
}