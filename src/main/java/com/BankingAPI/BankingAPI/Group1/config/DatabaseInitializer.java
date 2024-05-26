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
        Users joan = new Users("joan", "joan.doe@example.com", "Joan", "Doe", "12345673", "0123456789", LocalDate.of(1990, 1, 1), 5000.0, 1000.0, true, UserType.ROLE_CUSTOMER, bCryptPasswordEncoder.encode("joan"));
        Users newEmployee = new Users("Employee", "employee@example.com", "Em", "Yee", "1234567893", "01234567891", LocalDate.of(1990, 1, 1), 5000.0, 1000.0, true, UserType.ROLE_EMPLOYEE, bCryptPasswordEncoder.encode("employee"));
        userRepository.save(newUsers);
        userRepository.save(newUser);
        userRepository.save(joan);
        userRepository.save(newEmployee);

        Account newAccount = new Account(newUsers, "NL89INHO0044053200", "EUR", AccountType.CHECKING, true, 5000.0, 0.00);
        Account newAccounts = new Account(newUsers, "NL89INHO0044053203", "EUR", AccountType.SAVINGS, true, 8100.0, 0.00);

        Account joanAccount = new Account(joan, "NL89INHO0044053201", "EUR", AccountType.CHECKING, true, 7800.0, 0.00);
        Account joanAccounts = new Account(joan, "NL89INHO004523271", "EUR", AccountType.SAVINGS, true, 5000.0, 0.00);

        Account janeAccount =  new Account(newUser, "NL89INHO0044053282", "EUR", AccountType.CHECKING, true, 2800.0, 3000.0);
        Account janeAccounts =  new Account(newUser, "NL89INHO0044053283", "EUR", AccountType.SAVINGS, false, 200.0, 0.00);


        accountRepository.save(joanAccount);
        accountRepository.save(joanAccounts);
        accountRepository.save(newAccount);
        accountRepository.save(newAccounts);
        accountRepository.save(janeAccount);
        accountRepository.save(janeAccounts);



        Transaction ATMDeposit = new Transaction(newUsers, newAccount.getIBAN().toString(), "ATM", 9500.0, LocalDate.now());
        transactionRepository.save(ATMDeposit);

        Transaction newATMDeposit = new Transaction(newUsers, newAccount.getIBAN().toString(), "ATM", 100, LocalDate.now());
        transactionRepository.save(newATMDeposit);

        Transaction ATMWithdrawal = new Transaction(newUsers, "ATM", newAccount.getIBAN().toString(), 2800.0, LocalDate.now());
        transactionRepository.save(ATMWithdrawal);

        Transaction newATMWithdrawal = new Transaction(newUsers, "ATM", newAccount.getIBAN().toString(), 900.0, LocalDate.now());
        transactionRepository.save(newATMWithdrawal);

        Transaction onlineTransferByCustomer = new Transaction(newUsers, newAccount.getIBAN().toString(), newAccounts.getIBAN().toString(), 2800.0, LocalDate.now());
        transactionRepository.save(onlineTransferByCustomer);

        Transaction newOnlineTransferByCustomer = new Transaction(newUsers, newAccount.getIBAN().toString(), newAccounts.getIBAN().toString(), 300.0, LocalDate.now());
        transactionRepository.save(newOnlineTransferByCustomer);

        Transaction onlineTransferByEmployee = new Transaction(newEmployee, newAccount.getIBAN().toString(), joanAccount.getIBAN().toString(), 1000.0, LocalDate.now());
        transactionRepository.save(onlineTransferByEmployee);

        Transaction newOnlineTransferByEmployee = new Transaction(newEmployee, newAccount.getIBAN().toString(), joanAccount.getIBAN().toString(), 1800.0, LocalDate.now());
        transactionRepository.save(newOnlineTransferByEmployee);

    }


}