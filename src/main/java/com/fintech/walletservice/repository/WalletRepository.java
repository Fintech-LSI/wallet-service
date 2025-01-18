package com.fintech.walletservice.repository;


import com.fintech.walletservice.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findByUserId(Long userId);

    List<Wallet> findWalletsByUserId(Long userId);
}
