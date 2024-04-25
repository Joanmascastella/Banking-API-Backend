package com.BankingAPI.BankingAPI.Group1.model.dto;

import java.time.LocalDate;

public record TransactionDTO(String fromAccount, String toAccount, double amount, LocalDate date, int userId) {
}
