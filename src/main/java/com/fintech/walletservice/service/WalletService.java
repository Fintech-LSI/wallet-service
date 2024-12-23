package com.fintech.walletservice.service;

import com.fintech.walletservice.dto.requests.CreateWalletRequest;
import com.fintech.walletservice.dto.requests.UpdateBalanceRequest;
import com.fintech.walletservice.dto.responses.WalletResponse;
import com.fintech.walletservice.entity.Currency;
import com.fintech.walletservice.entity.Wallet;
import com.fintech.walletservice.exception.CurrencyException;
import com.fintech.walletservice.exception.WalletException;
import com.fintech.walletservice.repository.CurrencyRepository;
import com.fintech.walletservice.repository.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final CurrencyRepository currencyRepository;

    public WalletService(WalletRepository walletRepository, CurrencyRepository currencyRepository) {
        this.walletRepository = walletRepository;
        this.currencyRepository = currencyRepository;
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
