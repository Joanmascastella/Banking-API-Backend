package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.config.BeanFactory;
import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.AccountGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserDetailsDTO;
import com.BankingAPI.BankingAPI.Group1.repository.AccountRepository;
import com.BankingAPI.BankingAPI.Group1.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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


}
