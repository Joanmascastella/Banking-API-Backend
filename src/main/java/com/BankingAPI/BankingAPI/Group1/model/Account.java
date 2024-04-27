package com.BankingAPI.BankingAPI.Group1.model;

import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Account")
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int userId;
    private String IBAN;
    private String Currency;
    private AccountType accountType;
    private boolean isActive;
    private double balance;
    private double absoluteLimit;

    public Account(int userId, String IBAN, String currency, AccountType accountType, boolean isActive, double balance, double absoluteLimit) {
        this.userId = userId;
        this.IBAN = IBAN;
        Currency = currency;
        this.accountType = accountType;
        this.isActive = isActive;
        this.balance = balance;
        this.absoluteLimit = absoluteLimit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getIBAN() {
        return IBAN;
    }

    public void setIBAN(String IBAN) {
        this.IBAN = IBAN;
    }

    public String getCurrency() {
        return Currency;
    }

    public void setCurrency(String currency) {
        Currency = currency;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getAbsoluteLimit() {
        return absoluteLimit;
    }

    public void setAbsoluteLimit(double absoluteLimit) {
        this.absoluteLimit = absoluteLimit;
    }
}
