package com.fintech.walletservice.dto.responses;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private long timestamp;

    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}