package com.fintech.walletservice.controller;

import com.fintech.walletservice.entity.Currency;
import com.fintech.walletservice.service.CurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

  @GetMapping("/id/{code}")
  public ResponseEntity<Currency> getCurrencyById(@PathVariable Long id) {
    return ResponseEntity.ok(currencyService.getCurrencyById(id) );
  }

  @GetMapping
  public ResponseEntity<List<Currency>> getAllCurrencies() {
    return ResponseEntity.ok(currencyService.getAllCurrencies());
  }
}
