package com.BankingAPI.BankingAPI.Group1.model.dto;

import java.time.LocalDate;

public record TransactionGETPOSTResponseDTO(String fromAccount, String toAccount, double amount, LocalDate date, Long userId) {
}
