package com.BankingAPI.BankingAPI.Group1.config;

import com.BankingAPI.BankingAPI.Group1.model.*;
import com.BankingAPI.BankingAPI.Group1.repository.AccountRepository;
import com.BankingAPI.BankingAPI.Group1.repository.TransactionRepository;
import com.BankingAPI.BankingAPI.Group1.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DatabaseInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public DatabaseInitializer(UserRepository userRepository, TransactionRepository transactionRepository, AccountRepository accountRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
      this.initDatabase();
    }

    private void initDatabase() {
        User newUser = new User("johndoe", "john.doe@example.com", "John", "Doe", "123456789", "0123456789", LocalDate.of(1990, 1, 1), 5000.0, 1000.0, true, UserType.CUSTOMER, bCryptPasswordEncoder.encode("password123"));
        userRepository.save(newUser);

        Transaction newTransaction = new Transaction(1, "123456789", "123456789", 2000.0, LocalDate.now());
        transactionRepository.save(newTransaction);

        Account newAccount = new Account(1, "DE89 3704 0044 0532 0130 00", "EUR", AccountType.CHECKING, true, 5000.0, 0.00);
        accountRepository.save(newAccount);
    }


}
