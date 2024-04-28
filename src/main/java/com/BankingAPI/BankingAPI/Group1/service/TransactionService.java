package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.config.BeanFactory;
import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.repository.AccountRepository;
import com.BankingAPI.BankingAPI.Group1.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final BeanFactory beanFactory;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, BeanFactory beanFactory, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.beanFactory = beanFactory;
        this.accountRepository = accountRepository;
    }

    public List<TransactionGETPOSTResponseDTO> allTransactions() {
        try {
            beanFactory.validateAuthentication();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public TransactionGETPOSTResponseDTO transferToOtherCustomer(TransactionGETPOSTResponseDTO transactionDTO) throws Exception {
        beanFactory.validateAuthentication();

        Account fromAccount = getAccount(transactionDTO.fromAccount());
        Account toAccount = getAccount(transactionDTO.toAccount());

        if (fromAccount.getAccountType() == AccountType.SAVINGS && toAccount.getAccountType() == AccountType.SAVINGS) {
            throw new Exception("Cannot transfer money between savings accounts.");
        }

        if (fromAccount.getUser().getId() == toAccount.getUser().getId()) {
            throw new Exception("Both accounts cannot belong to the same user for this operation.");
        }

        validateTransactionLimits(fromAccount, transactionDTO.amount());
        performTransfer(fromAccount, toAccount, transactionDTO.amount());

        Transaction newTransaction = createAndSaveTransaction(fromAccount, toAccount, transactionDTO.amount());
        return mapToTransactionResponse(newTransaction);
    }


    public TransactionGETPOSTResponseDTO transferMoneyToOwnAccount(TransactionGETPOSTResponseDTO transactionDTO) throws Exception {
        beanFactory.validateAuthentication();

        Account fromAccount = getAccount(transactionDTO.fromAccount());
        Account toAccount = getAccount(transactionDTO.toAccount());

        validateAccountOwnership(fromAccount, toAccount);
        validateTransactionLimits(fromAccount, transactionDTO.amount());

        performTransfer(fromAccount, toAccount, transactionDTO.amount());
        Transaction newTransaction = createAndSaveTransaction(fromAccount, toAccount, transactionDTO.amount());

        return mapToTransactionResponse(newTransaction);
    }



    private Account getAccount(String iban) throws Exception {
        Account account = accountRepository.findByIBAN(iban)
                .orElseThrow(() -> new Exception("Account with IBAN: " + iban + " not found"));

        if (account.getUser() == null) {
            throw new Exception("No user associated with the account: " + iban);
        }
        return account;
    }

    private void validateAccountOwnership(Account fromAccount, Account toAccount) throws Exception {
        Long currentUserId = beanFactory.getCurrentUserId();
        if (fromAccount.getUser().getId() != currentUserId || toAccount.getUser().getId() != currentUserId) {
            throw new Exception("Both accounts must belong to the same user");
        }
    }

    private void validateTransactionLimits(Account fromAccount, double amount) throws Exception {
        if (fromAccount.getBalance() < amount) {
            throw new Exception("Insufficient funds");
        }
        if (amount > fromAccount.getUser().getDailyLimit()) {
            throw new Exception("Transaction exceeds daily limit");
        }
    }

    private void performTransfer(Account fromAccount, Account toAccount, double amount) {
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    private Transaction createAndSaveTransaction(Account fromAccount, Account toAccount, double amount) {
        Transaction transaction = new Transaction();
        transaction.setUserId(beanFactory.getCurrentUserId());
        transaction.setFromAccount(fromAccount.getIBAN());
        transaction.setToAccount(toAccount.getIBAN());
        transaction.setAmount(amount);
        transaction.setDate(LocalDate.now());
        return transactionRepository.save(transaction);
    }

    private TransactionGETPOSTResponseDTO mapToTransactionResponse(Transaction transaction) {
        return new TransactionGETPOSTResponseDTO(
                transaction.getFromAccount(),
                transaction.getToAccount(),
                transaction.getAmount(),
                transaction.getDate(),
                (int) transaction.getUserId()
        );
    }
}
