package com.fintech.walletservice.controller;

import com.fintech.walletservice.dto.requests.CreateWalletRequest;
import com.fintech.walletservice.dto.requests.UpdateBalanceRequest;
import com.fintech.walletservice.dto.responses.WalletResponse;
import com.fintech.walletservice.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/wallets")

public class WalletController {
    private final WalletService walletService;
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
      return ResponseEntity.ok("wallet is runing");
    }


    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@RequestBody CreateWalletRequest request) {
        return ResponseEntity.ok(walletService.createWallet(request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getWalletDetails(id));
    }
    @PostMapping("/{id}/file")
    public ResponseEntity<String> createWalletFile(@PathVariable Long id) {
        String fileName = walletService.createFileWithUserName(id);
        return ResponseEntity.ok("File created: " + fileName);
    }

    @PutMapping("/{id}/balance")
    public ResponseEntity<WalletResponse> updateBalance(
            @PathVariable Long id,
            @RequestBody UpdateBalanceRequest request) {
        return ResponseEntity.ok(walletService.updateBalance(id, request));
    }
}
