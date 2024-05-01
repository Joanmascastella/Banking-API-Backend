package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.repository.TransactionRepository;
import com.BankingAPI.BankingAPI.Group1.repository.specification.TransactionSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {


    private final TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;

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
                        transaction.getUserId()
                ))
                .collect(Collectors.toList());
    }
    public List<TransactionGETPOSTResponseDTO> getTransactionsByUserId(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream()
                .map(transaction -> new TransactionGETPOSTResponseDTO(
                        transaction.getFromAccount(),
                        transaction.getToAccount(),
                        transaction.getAmount(),
                        transaction.getDate(),
                       transaction.getUserId()
                ))
                .collect(Collectors.toList());
    }

    public TransactionGETPOSTResponseDTO processWithdrawal(TransactionGETPOSTResponseDTO transactionDTO) throws IllegalArgumentException, IllegalStateException {
        Account account = accountService.findById(transactionDTO.userId());
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        Users user = userService.findById(transactionDTO.userId());
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (!userService.checkAndUpdateDailyLimit(user, transactionDTO.amount())) {
            throw new IllegalStateException("Daily limit exceeded");
        }

        double newBalance = account.getBalance() - transactionDTO.amount();
        if (newBalance < account.getAbsoluteLimit()) {
            throw new IllegalStateException("Withdrawal exceeds absolute limit");
        }

        account.setBalance(newBalance);
        accountService.save(account);


        Transaction transaction = new Transaction(
                transactionDTO.userId(),
                account.getIBAN(),
                "ATM",
                transactionDTO.amount(),
                transactionDTO.date()
        );
        this.save(transaction);

        return new TransactionGETPOSTResponseDTO(
                account.getIBAN(),
                "ATM",
                transactionDTO.amount(),
                transactionDTO.date(),
                transactionDTO.userId()
        );
    }

    public TransactionGETPOSTResponseDTO processDeposit(TransactionGETPOSTResponseDTO transactionDTO) throws IllegalArgumentException {

        Account account = accountService.findById(transactionDTO.userId());
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        double newBalance = account.getBalance() + transactionDTO.amount();
        account.setBalance(newBalance);
        accountService.save(account);
        Transaction transaction = new Transaction(
                transactionDTO.userId(),
                "ATM",
                account.getIBAN(),
                transactionDTO.amount(),
                transactionDTO.date()
        );
        this.save(transaction);
        return new TransactionGETPOSTResponseDTO(
                "ATM",
                account.getIBAN(),
                transactionDTO.amount(),
                transactionDTO.date(),
                transactionDTO.userId()
        );
    }

    public List<Transaction> filterTransactions(String IBAN, Double amount, Double amountGreater, Double amountLess, LocalDate startDate, LocalDate endDate) {
        Specification<Transaction> spec = Specification.where(null);

        if (IBAN != null) {
            spec = spec.and(TransactionSpecification.hasIBAN(IBAN));
        }
        if (amount != null) {
            spec = spec.and(TransactionSpecification.amountEquals(amount));
        }
        if (amountGreater != null) {
            spec = spec.and(TransactionSpecification.amountGreaterThan(amountGreater));
        }
        if (amountLess != null) {
            spec = spec.and(TransactionSpecification.amountLessThan(amountLess));
        }
        if (startDate != null && endDate != null) {
            spec = spec.and(TransactionSpecification.isBetweenDates(startDate, endDate));
        }

        return transactionRepository.findAll(spec);
    }
    public void save(Transaction transaction) {
        transactionRepository.save(transaction);
    }
}
