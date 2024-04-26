package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.dto.AccountGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<AccountGETPOSTResponseDTO> getAllAccounts(){
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(account -> new AccountGETPOSTResponseDTO(
                        account.getUserId(),
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
