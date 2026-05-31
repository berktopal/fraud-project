package com.berqbank.fraudengine.controller;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionRequest {
    private String accountNumber;       // Gönderen Hesap (Bizim müşterimiz)
    private String targetAccountNumber; // Alıcı İBAN
    private BigDecimal amount;          // İşlem Tutarı
    private String ipAddress;           // İstek yapılan IP
}