package com.BankingAPI.BankingAPI.Group1.repository;

import com.BankingAPI.BankingAPI.Group1.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
