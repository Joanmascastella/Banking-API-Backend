package com.BankingAPI.BankingAPI.Group1.config;

import com.BankingAPI.BankingAPI.Group1.model.*;
import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;
import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import com.BankingAPI.BankingAPI.Group1.repository.AccountRepository;
import com.BankingAPI.BankingAPI.Group1.repository.TransactionRepository;
import com.BankingAPI.BankingAPI.Group1.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

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
        Users newUsers = new Users("johndoe", "john.doe@example.com", "John", "Doe", "123456789", "0123456789", LocalDate.of(1990, 1, 1), 5000.0, 1000.0, true, UserType.ROLE_CUSTOMER, bCryptPasswordEncoder.encode("123"));
        Users newUser = new Users("janedoe", "jane@doe.com", "Jane", "Doe", "123789456", "0987654321", LocalDate.of(1998, 4, 14), 0, 0, false, UserType.ROLE_CUSTOMER, bCryptPasswordEncoder.encode("user"));
        Users newEmployee = new Users("Employee", "employee@example.com", "Em", "Yee", "1234567893", "01234567891", LocalDate.of(1990, 1, 1), 5000.0, 1000.0, true, UserType.ROLE_EMPLOYEE, bCryptPasswordEncoder.encode("employee"));
        userRepository.save(newUsers);
        userRepository.save(newUser);
        userRepository.save(newEmployee);

        Transaction newTransaction = new Transaction(newUsers, "123456789", "123456789", 2000.0, LocalDate.now());
        transactionRepository.save(newTransaction);

        Account newAccount = new Account(newUsers, "DE89 INHO 0044 0532 00", "EUR", AccountType.CHECKING, true, 5000.0, 0.00);
        accountRepository.save(newAccount);
    }


}
