package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {


    private final TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> allTransactions(){
        return transactionRepository.findAll();
    }
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public Transaction processWithdrawal(Transaction transaction) throws IllegalArgumentException, IllegalStateException {
        Account account = accountService.findById(transaction.getUser().getId());
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        if (!accountService.checkAndUpdateDailyLimit(transaction.getUser(), transaction.getAmount())) {
            throw new IllegalStateException("Daily limit exceeded");
        }

        double newBalance = account.getBalance() - transaction.getAmount();
        if (newBalance < account.getAbsoluteLimit()) {
            throw new IllegalStateException("Withdrawal exceeds absolute limit");
        }

        account.setBalance(newBalance);
        accountService.save(account);

        transaction.setFromAccount(account.getIBAN());  // Set the user's account IBAN for withdrawal
        return transactionRepository.save(transaction);
    }

    public Transaction processDeposit(Transaction transaction) {
        Account account = accountService.findById(transaction.getUser().getId());
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        double newBalance = account.getBalance() + transaction.getAmount();
        account.setBalance(newBalance);
        accountService.save(account);

        transaction.setToAccount(account.getIBAN());
        return transactionRepository.save(transaction);
    }
}
