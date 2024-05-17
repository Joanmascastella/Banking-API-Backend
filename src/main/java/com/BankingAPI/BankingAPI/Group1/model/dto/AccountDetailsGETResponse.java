package com.BankingAPI.BankingAPI.Group1.model.dto;

import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;

import java.time.LocalDate;

public record AccountDetailsGETResponse(String username, String email, String firstName, String lastName, String BSN, String phoneNumber, LocalDate birthDate, String IBAN, String currency, AccountType accountType, double balance, double absoluteLimit) {
}
