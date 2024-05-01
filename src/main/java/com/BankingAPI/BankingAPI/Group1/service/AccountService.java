package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.config.BeanFactory;
import com.BankingAPI.BankingAPI.Group1.model.Account;

import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.AccountGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserDetailsDTO;
import com.BankingAPI.BankingAPI.Group1.repository.AccountRepository;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;


import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final BeanFactory beanFactory;



    public AccountService(AccountRepository accountRepository, BeanFactory beanFactory) {
        this.accountRepository = accountRepository;
        this.beanFactory = beanFactory;
    }

    public List<AccountGETPOSTResponseDTO> getAllAccounts(){
        try {
            beanFactory.validateAuthentication();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(account -> new AccountGETPOSTResponseDTO(
                        account.getUser().getId(),
                        account.getIBAN(),
                        account.getCurrency(),
                        account.getAccountType(),
                        account.isActive(),
                        account.getBalance(),
                        account.getAbsoluteLimit()
                ))
                .collect(Collectors.toList());
    }

    public UserDetailsDTO getAccountDetails(Long userId) throws Exception {
        try {
            beanFactory.validateAuthentication();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Optional<Account> accountOptional = accountRepository.findByUserId(userId);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            Users user = account.getUser();
            return new UserDetailsDTO(
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getBSN(),
                    user.getPhoneNumber(),
                    user.getBirthDate(),
                    user.getTotalBalance(),
                    user.getDailyLimit(),
                    account.getIBAN(),
                    account.getBalance(),
                    account.getAbsoluteLimit()
            );
        } else {
            throw new Exception("No account found for user ID: " + userId);
        }
    }

    public void createAccountsForUser(Users user, double absoluteSavingLimit, double absoluteCheckingLimit) {
        createCheckingAccount(user, absoluteCheckingLimit, absoluteSavingLimit);
        createSavingsAccount(user, absoluteCheckingLimit, absoluteSavingLimit);
    }

    private void createSavingsAccount(Users user, double absoluteSavingLimit, double absoluteCheckingLimit) {
        createAccount(user, AccountType.SAVINGS, absoluteSavingLimit);
    }

    private void createCheckingAccount(Users user, double absoluteSavingLimit, double absoluteCheckingLimit) {
        createAccount(user, AccountType.CHECKING, absoluteCheckingLimit);
    }

    private void createAccount(Users user, AccountType accountType, double absoluteLimit) {
        Account account = new Account();
        account.setUser(user);
        account.setIBAN(generateIBAN());
        account.setCurrency("€");
        account.setAccountType(accountType);
        account.setActive(true);
        account.setBalance(0.00);
        account.setAbsoluteLimit(absoluteLimit);
        accountRepository.save(account);
    }
    private String generateIBAN() {
        String iban;
        do{
            String firstDigits = String.format("%02d", new Random().nextInt(100));

            String lastDigits = String.format("%010d", new Random().nextInt(1000000000));

            iban = "NL" + firstDigits + "INH00" + lastDigits;
        } while(ibanExists(iban));

        return iban;
    }

    private boolean ibanExists(String iban) {
        return accountRepository.existsByIBAN(iban);
    }

    public void updateAccount(Account account) {
        Account currentAccount = accountRepository.findById(account.getId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found."));
        currentAccount.setAbsoluteLimit(account.getAbsoluteLimit());
        accountRepository.save(currentAccount);
    }

    public void closeAccount(long accountId) {
        Account currentAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found."));
        currentAccount.setActive(false);
        accountRepository.save(currentAccount);
    }
    public Account findById(Long accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }


    public void save(Account account) {
        accountRepository.save(account);
    }
    public List<AccountGETPOSTResponseDTO> findSavingsAccountsByUserId(long userId) {
        List<Account> accounts = accountRepository.findSavingsAccountsByUserId(userId);
        return accounts.stream()
                .map(account -> new AccountGETPOSTResponseDTO(
                        account.getUser().getId(),
                        account.getIBAN(),
                        account.getCurrency(),
                        account.getAccountType(),
                        account.isActive(),
                        account.getBalance(),
                        account.getAbsoluteLimit()
                ))
                .collect(Collectors.toList());
    }

    public List<AccountGETPOSTResponseDTO> findCheckingAccountsByUserId(long userId) {
        List<Account> accounts = accountRepository.findCheckingAccountsByUserId(userId);
        return accounts.stream()
                .map(account -> new AccountGETPOSTResponseDTO(
                        account.getUser().getId(),
                        account.getIBAN(),
                        account.getCurrency(),
                        account.getAccountType(),
                        account.isActive(),
                        account.getBalance(),
                        account.getAbsoluteLimit()
                ))
                .collect(Collectors.toList());
    }


    public List<AccountGETPOSTResponseDTO> findByAbsoluteLimit(double absoluteLimit) {
        List<Account> accounts = accountRepository.findByAbsoluteLimit(absoluteLimit);
        return accounts.stream()
                .map(account -> new AccountGETPOSTResponseDTO(
                        account.getUser().getId(),
                        account.getIBAN(),
                        account.getCurrency(),
                        account.getAccountType(),
                        account.isActive(),
                        account.getBalance(),
                        account.getAbsoluteLimit()
                ))
                .collect(Collectors.toList());
    }

    public List<AccountGETPOSTResponseDTO> findByInactiveTag() {
        List<Account> accounts = accountRepository.findByInactiveTag();
        return accounts.stream()
                .map(account -> new AccountGETPOSTResponseDTO(
                        account.getUser().getId(),
                        account.getIBAN(),
                        account.getCurrency(),
                        account.getAccountType(),
                        account.isActive(),
                        account.getBalance(),
                        account.getAbsoluteLimit()
                ))
                .collect(Collectors.toList());
    }


}
