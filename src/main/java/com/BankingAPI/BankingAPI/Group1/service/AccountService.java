package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.User;
import com.BankingAPI.BankingAPI.Group1.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAllAccounts(){
        return accountRepository.findAll();
    }
    public Account findById(Long accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    public boolean checkAndUpdateDailyLimit(User user, double amount) {
        double updatedAmount = user.getDailyLimit() - amount;
        if (updatedAmount < 0) {
            return false;
        }
        user.setDailyLimit(updatedAmount);
        return true;
    }

    public void save(Account account) {
        accountRepository.save(account);
    }
}
