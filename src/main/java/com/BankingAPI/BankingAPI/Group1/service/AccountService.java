package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.AccountGETPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserDetailsDTO;
import com.BankingAPI.BankingAPI.Group1.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
