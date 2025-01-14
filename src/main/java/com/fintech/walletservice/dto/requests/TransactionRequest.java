package com.fintech.walletservice.dto.requests;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest implements Serializable {
  private String transactionType; // DEPOSIT , TRANSFER , WITHDRAW
  private Long walletId;
  private Long targetWalletId;
  private Double amount;
  private String description;
  private String moneyMethod;
}
