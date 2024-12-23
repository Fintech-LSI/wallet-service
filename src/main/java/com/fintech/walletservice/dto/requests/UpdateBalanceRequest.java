package com.fintech.walletservice.dto.requests;

public record UpdateBalanceRequest(Double amount, String currencyCode) {}
