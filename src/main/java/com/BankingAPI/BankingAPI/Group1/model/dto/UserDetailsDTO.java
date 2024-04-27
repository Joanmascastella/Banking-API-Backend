package com.BankingAPI.BankingAPI.Group1.model.dto;

import java.time.LocalDate;


public record UserDetailsDTO(String username, String email, String firstName, String lastName, String BSN, String phoneNumber, LocalDate birthDate, double totalBalance, double dailyLimit, String IBAN, double balance, double absoluteLimit) {
}
