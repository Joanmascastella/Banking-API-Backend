package com.BankingAPI.BankingAPI.Group1.model.dto;

public record TransferMoneyPOSTResponse(String fromAccount, String toAccount, double amount) {
}
