package com.berqbank.fraudengine.entity;

public enum TransactionStatus {
    PENDING,    // Kurallar işletiliyor
    APPROVED,   // Güvenli, onaylandı
    BLOCKED,    // Doğrudan engellendi
    FLAGGED     // Şüpheli, denetçi onay bekliyor
}