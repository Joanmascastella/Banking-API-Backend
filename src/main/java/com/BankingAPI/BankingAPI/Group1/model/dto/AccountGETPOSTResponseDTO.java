package com.BankingAPI.BankingAPI.Group1.model.dto;

import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;


public record AccountGETPOSTResponseDTO(Long userId, String IBAN, String currency, AccountType accountType, boolean isActive, double balance, double absoluteLimit) {
}
