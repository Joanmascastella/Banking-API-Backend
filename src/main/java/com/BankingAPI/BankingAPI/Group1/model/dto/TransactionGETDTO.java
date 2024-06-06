package com.BankingAPI.BankingAPI.Group1.model.dto;

import java.time.LocalDate;

public record TransactionGETDTO(Long id, String fromAccount, String toAccount, double amount, LocalDate date,
                                Long userId) {

    public TransactionGETDTO(String fromAccount, String toAccount, double amount, LocalDate date, Long userId)
    {
        this(null, fromAccount, toAccount, amount, date, userId);

    }


}
