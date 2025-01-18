package com.fintech.walletservice.service;

import com.fintech.walletservice.dto.requests.CreateWalletRequest;
import com.fintech.walletservice.dto.requests.TransactionRequest;
import com.fintech.walletservice.dto.requests.UpdateBalanceRequest;
import com.fintech.walletservice.dto.responses.UserResponse;
import com.fintech.walletservice.dto.responses.WalletResponse;
import com.fintech.walletservice.entity.Currency;
import com.fintech.walletservice.entity.Wallet;
import com.fintech.walletservice.exception.CurrencyException;
import com.fintech.walletservice.exception.WalletException;
import com.fintech.walletservice.repository.CurrencyRepository;
import com.fintech.walletservice.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WalletService {

    @Value("${var.service.user-service:http://localhost:8080/user-service}")
    private String userServiceUrl ; // User Service base URL


  private final WalletRepository walletRepository;
  private final CurrencyRepository currencyRepository;
  private final RestTemplate restTemplate;
  private final TransactionProducerService transactionProducer;

    public WalletService(
      WalletRepository walletRepository,
      CurrencyRepository currencyRepository,
      RestTemplate restTemplate,
      TransactionProducerService transactionProducer

    ) {
        this.walletRepository = walletRepository;
        this.currencyRepository = currencyRepository;
        this.restTemplate = restTemplate;
        this.transactionProducer = transactionProducer;
    }

  public WalletResponse deposit(TransactionRequest request) {
    Wallet sourceWallet = walletRepository.findById(request.getWalletId())
      .orElseThrow(() -> new WalletException("Source wallet not found"));

    if (!request.getTransactionType().equals("DEPOSIT")) {
      throw new WalletException("Invalid transaction type : you should use DEPOSIT");
    }

    sourceWallet.setBalance(sourceWallet.getBalance() + request.getAmount());

    walletRepository.save(sourceWallet);


    transactionProducer.sendTransactionRequest(request);

    return mapToWalletResponse(sourceWallet);

  }

  public WalletResponse transfer(TransactionRequest request) {
    // 1. Validate wallets and check balance
    Wallet sourceWallet = walletRepository.findById(request.getWalletId())
      .orElseThrow(() -> new WalletException("Source wallet not found"));
    Wallet targetWallet = walletRepository.findById(request.getTargetWalletId())
      .orElseThrow(() -> new WalletException("Target wallet not found"));

    if (sourceWallet.getBalance() < request.getAmount()) {
      throw new WalletException("Insufficient balance");
    }

    if (!request.getTransactionType().equals("TRANSFER")) {
      throw new WalletException("Invalid transaction type : you should use TRANSFER");
    }

    double targetAmount = request.getAmount();
    if (targetWallet.getCurrency().getId() != sourceWallet.getCurrency().getId()) {
      double MoneyInDollar = request.getAmount() * sourceWallet.getCurrency().getExchangeRate();
      targetAmount = MoneyInDollar / targetWallet.getCurrency().getExchangeRate();
    }

    // 3. Update balances
    sourceWallet.setBalance(sourceWallet.getBalance() - request.getAmount());
    targetWallet.setBalance(targetWallet.getBalance() + targetAmount );

    // 4. Save wallet changes
    walletRepository.save(sourceWallet);
    walletRepository.save(targetWallet);

    // 5. Send to transaction service for recording
    transactionProducer.sendTransactionRequest(request);

    return mapToWalletResponse(sourceWallet);
  }


  public WalletResponse withdraw(TransactionRequest request) {
    // 1. Validate wallets and check balance
    Wallet sourceWallet = walletRepository.findById(request.getWalletId())
      .orElseThrow(() -> new WalletException("Source wallet not found"));


    if (!request.getTransactionType().equals("WITHDRAW")) {
      throw new WalletException("Invalid transaction type : you should use WITHDRAW");
    }
    if (sourceWallet.getBalance() < request.getAmount()) {
      throw new WalletException("Insufficient balance");
    }

    // 3. Update balances
    sourceWallet.setBalance(sourceWallet.getBalance() - request.getAmount());


    // 4. Save wallet changes
    walletRepository.save(sourceWallet);

    // 5. Send to transaction service for recording
    transactionProducer.sendTransactionRequest(request);

    return mapToWalletResponse(sourceWallet);
  }

  public List<WalletResponse> getWalletsByUserId(Long userId) {
      List<Wallet> wallets = walletRepository.findWalletsByUserId(userId);
      return wallets.stream().map(this::mapToWalletResponse).collect(Collectors.toList());
  }

  public WalletResponse getWalletDetails(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletException("Wallet not found"));

        // Fetch user details from User Service
        String userName = fetchUserName(wallet.getUserId());

        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currencyCode(wallet.getCurrency().getCode())
                .currencyName(wallet.getCurrency().getName())
                .userName(userName) // Include userName in response
                .build();
    }

  private String fetchUserName(Long userId) {
      try {
          ResponseEntity<UserResponse> response = restTemplate.getForEntity(userServiceUrl + "/" + userId, UserResponse.class);
          return response.getBody().getFirstName() + " " + response.getBody().getLastName();
      } catch (Exception e) {
          throw new WalletException("Failed to fetch user details: " + e.getMessage());
      }
  }

  public WalletResponse createWallet(CreateWalletRequest request) {
      Currency currency = currencyRepository.findByCode(request.currencyCode())
              .orElseThrow(() -> new CurrencyException("Currency not found"));

      Wallet wallet = new Wallet();
      wallet.setUserId(request.userId());
      wallet.setCurrency(currency);
      wallet.setBalance(request.initialBalance());

      wallet = walletRepository.save(wallet);
      return mapToWalletResponse(wallet);
  }

  public String createFileWithUserName(Long walletId) {
      Wallet wallet = walletRepository.findById(walletId)
              .orElseThrow(() -> new WalletException("Wallet not found"));

      String userName = restTemplate.getForObject(userServiceUrl + "/api/users/" + wallet.getUserId() + "/name", String.class);

      String fileName = "wallet_" + walletId + "_" + userName.replace(" ", "_") + ".txt";
      Path filePath = Paths.get("files/" + fileName);

      try {
          Files.writeString(filePath, "Wallet Info:\n" +
                  "User: " + userName + "\n" +
                  "Balance: " + wallet.getBalance() + "\n" +
                  "Currency: " + wallet.getCurrency().getName());
      } catch (IOException e) {
          throw new RuntimeException("Error creating file", e);
      }

      return fileName;
  }

  public WalletResponse updateBalance(Long walletId, UpdateBalanceRequest request) {
      Wallet wallet = walletRepository.findById(walletId)
              .orElseThrow(() -> new WalletException("Wallet not found"));

      if (request.amount() + wallet.getBalance() < 0) {
          throw new WalletException("Insufficient balance");
      }

      wallet.setBalance(wallet.getBalance() + request.amount());
      wallet = walletRepository.save(wallet);
      return mapToWalletResponse(wallet);
  }

  private WalletResponse mapToWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currencyCode(wallet.getCurrency().getCode())
                .currencyName(wallet.getCurrency().getName())
                .build();
    }

  public List<Wallet> getAllWallets() {
    return walletRepository.findAll();
  }

}
