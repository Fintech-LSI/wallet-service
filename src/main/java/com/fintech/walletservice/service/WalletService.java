package com.fintech.walletservice.service;

import com.fintech.walletservice.dto.requests.CreateWalletRequest;
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

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final CurrencyRepository currencyRepository;
    private final RestTemplate restTemplate;
    private final String userServiceUrl = "http://localhost:8090/api/users"; // User Service base URL

    public WalletService(WalletRepository walletRepository, CurrencyRepository currencyRepository, RestTemplate restTemplate) {
        this.walletRepository = walletRepository;
        this.currencyRepository = currencyRepository;
        this.restTemplate = restTemplate;
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
}
