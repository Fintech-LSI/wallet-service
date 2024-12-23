package com.fintech.walletservice.dto.requests;


public record CreateWalletRequest(Long userId, String currencyCode, Double initialBalance) {}
