package com.fintech.walletservice.service;

import com.fintech.walletservice.entity.Currency;
import com.fintech.walletservice.exception.CurrencyException;
import com.fintech.walletservice.repository.CurrencyRepository;
import org.springframework.stereotype.Service;
@Service
public class CurrencyService {
    private final CurrencyRepository currencyRepository;
    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }
    public Currency addCurrency(Currency currency) {
        return currencyRepository.save(currency);
    }

    public Currency getCurrency(String code) {
        return currencyRepository.findByCode(code)
                .orElseThrow(() -> new CurrencyException("Currency not found for code: " + code));
    }

}
