package com.fintech.walletservice.dto.responses;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class WalletResponse {
    private Long id;
    private Long userId;
    private Double balance;
    private String currencyCode;
    private String currencyName;
    private String userName; // Add userName
}