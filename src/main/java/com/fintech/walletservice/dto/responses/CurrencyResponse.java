package com.fintech.walletservice.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrencyResponse {
    private Long id;
    private String name;
    private String code;
    private Double exchangeRate;
}