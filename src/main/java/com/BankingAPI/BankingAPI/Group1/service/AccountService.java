package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.config.BeanFactory;
import com.BankingAPI.BankingAPI.Group1.exception.IBANGenerationException;
import com.BankingAPI.BankingAPI.Group1.exception.InvalidLimitException;
import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.AccountGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserApprovalDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserDetailsDTO;
import com.BankingAPI.BankingAPI.Group1.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

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

    public void createAccountsForUser(Users user, UserApprovalDTO approvalDTO) throws DataAccessException, IBANGenerationException{
        try {
            createCheckingAccount(user, approvalDTO.absoluteCheckingLimit());
            createSavingsAccount(user, approvalDTO.absoluteSavingLimit());
        } catch (DataAccessException e) {
            throw e;
        } catch (IBANGenerationException e) {
            throw new IBANGenerationException(e.getMessage());
        }
    }

    private void createSavingsAccount(Users user, Double absoluteSavingLimit) throws DataAccessException, IBANGenerationException {
        createAccount(user, AccountType.SAVINGS, absoluteSavingLimit);
    }

    private void createCheckingAccount(Users user, Double absoluteCheckingLimit) throws DataAccessException, IBANGenerationException {
        createAccount(user, AccountType.CHECKING, absoluteCheckingLimit);
    }

    private void createAccount(Users user, AccountType accountType, Double absoluteLimit) throws DataAccessException, IBANGenerationException {
        try {
            Account account = new Account();
            account.setUser(user);
            account.setIBAN(generateIBAN());
            account.setCurrency("€");
            account.setAccountType(accountType);
            account.setActive(true);
            account.setBalance(0.00);
            account.setAbsoluteLimit(absoluteLimit);
            accountRepository.save(account);
        } catch (DataAccessException e) {
            throw e;
        } catch (IBANGenerationException e) {
            throw new IBANGenerationException(e.getMessage());
        }
    }
    private String generateIBAN() throws IBANGenerationException{
        int generationAttempts = 0;
        String iban;
        do{
            generationAttempts++;
            if(generationAttempts > 100) {
                throw new IBANGenerationException("Failed to generate a unique IBAN.");
            }
            String firstDigits = String.format("%02d", new Random().nextInt(100));

            String lastDigits = String.format("%09d", new Random().nextInt(1000000000));

            iban = "NL" + firstDigits + "INHO0" + lastDigits;

        } while(ibanExists(iban));

        return iban;
    }

    private boolean ibanExists(String iban) {
        return accountRepository.existsByIBAN(iban);
    }

    public void updateAccount(String IBAN, AccountGETPOSTResponseDTO account) throws EntityNotFoundException, InvalidLimitException {
        Account currentAccount = accountRepository.findByIBAN(IBAN)
                .orElseThrow(() -> new EntityNotFoundException("Account not found by IBAN: " + IBAN));
        if (account.absoluteLimit() == null){
            throw new InvalidLimitException("Can't leave absolute limit empty. Please enter a valid amount.");
        }
        currentAccount.setAbsoluteLimit(account.absoluteLimit());
        accountRepository.save(currentAccount);
    }

    public void closeAccounts(long userId) throws EntityNotFoundException{
        List<Account> accounts = accountRepository.findAccountsByUserId(userId);
        if (accounts.isEmpty()) {
            throw new EntityNotFoundException("No accounts found for user with ID: " + userId);
        }
        for (Account account: accounts) {
            account.setActive(false);
            accountRepository.save(account);
        }
    }

    public Account findById(Long accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }


    public void save(Account account) {
        accountRepository.save(account);
    }

    public List<AccountGETPOSTResponseDTO> getAllCustomerAccounts() throws Exception{
        beanFactory.validateAuthentication();
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(account -> new AccountGETPOSTResponseDTO(
                        account.getId(),
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


    public List<AccountGETPOSTResponseDTO> findByAbsoluteLimit(double absoluteLimit) throws Exception {
        beanFactory.validateAuthentication();
        List<Account> accounts = accountRepository.findByAbsoluteLimit(absoluteLimit);
        return accounts.stream()
                .map(account -> new AccountGETPOSTResponseDTO(
                        account.getId(),
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

    public List<AccountGETPOSTResponseDTO> findByInactiveTag() throws Exception {
        beanFactory.validateAuthentication();
        List<Account> accounts = accountRepository.findByInactiveTag();
        return accounts.stream()
                .map(account -> new AccountGETPOSTResponseDTO(
                        account.getId(),
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

    public List<Account> getAccountsByUserId(long userId) {
        return accountRepository.findAccountsByUserId(userId);
    }
}
