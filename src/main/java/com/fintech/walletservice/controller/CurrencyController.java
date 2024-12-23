package com.fintech.walletservice.controller;

import com.fintech.walletservice.entity.Currency;
import com.fintech.walletservice.service.CurrencyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @PostMapping
    public Currency addCurrency(@RequestBody Currency currency) {
        return currencyService.addCurrency(currency);
    }

    @GetMapping("/{code}")
    public Currency getCurrency(@PathVariable String code) {
        return currencyService.getCurrency(code);
    }
}
