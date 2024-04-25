package com.BankingAPI.BankingAPI.Group1.model.dto;

import com.BankingAPI.BankingAPI.Group1.model.AccountType;


public record AccountDTO(int userId, String IBAN, String Currency, AccountType accountType, boolean isActive, double balance, double absoluteLimit) {

}
