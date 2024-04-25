package com.BankingAPI.BankingAPI.Group1.model.dto;

import com.BankingAPI.BankingAPI.Group1.model.AccountType;
import com.BankingAPI.BankingAPI.Group1.model.User;
import jakarta.persistence.JoinColumn;

public record AccountDTO(User user, String IBAN, String Currency, AccountType accountType, boolean isActive, double balance, double absoluteLimit) {

}
