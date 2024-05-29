package com.BankingAPI.BankingAPI.Group1.model;

import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Account")
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Users user;  // Relationship to Users class

    @Column(unique = true)
    @JsonProperty("IBAN")
    private String IBAN;

    @JsonProperty("currency")
    private String currency;
    @JsonProperty("accountType")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("balance")
    private double balance;

    @JsonProperty("absoluteLimit")
    private double absoluteLimit;

    public Account(Users user, String IBAN, String currency, AccountType accountType, boolean isActive, double balance, double absoluteLimit) {
        this.user = user;
        this.IBAN = IBAN;
        this.currency = currency;
        this.accountType = accountType;
        this.isActive = isActive;
        this.balance = balance;
        this.absoluteLimit = absoluteLimit;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getIBAN() {
        return IBAN;
    }

    public void setIBAN(String IBAN) {
        this.IBAN = IBAN;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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
        this.isActive = active;
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
