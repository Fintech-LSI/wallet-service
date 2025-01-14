package com.fintech.walletservice.service;

import com.fintech.walletservice.entity.Currency;
import com.fintech.walletservice.exception.CurrencyException;
import com.fintech.walletservice.repository.CurrencyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<Currency> getAllCurrencies() {
      return currencyRepository.findAll();
    }

    public Currency getCurrencyById(Long id) {
      return currencyRepository.findById(id).orElseThrow(() -> new CurrencyException("Currency not found for id: " + id));
    }
}
