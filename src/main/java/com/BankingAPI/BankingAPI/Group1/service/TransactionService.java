package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionGETPOSTResponseDTO> allTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        (int) transaction.getUserId()
                ))
                .collect(Collectors.toList());
    }
}
