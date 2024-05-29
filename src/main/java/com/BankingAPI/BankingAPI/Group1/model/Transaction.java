package com.BankingAPI.BankingAPI.Group1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "Transaction")
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @JsonProperty("fromAccount")
    private String fromAccount;

    @JsonProperty("toAccount")
    private String toAccount;

    @JsonProperty("Amount")
    private double amount;

    @JsonProperty("date")
    private LocalDate date;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    public Transaction(Users user, String fromAccount, String toAccount, double amount, LocalDate date) {
        this.user = user;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public boolean isFromAccountDifferentThanToAccount() {
        return !this.fromAccount.equals(this.toAccount);
    }

    public boolean isAmountPositive() {
        return this.amount > 0;
    }

    public boolean isAmountNegative() {
        return this.amount < 0;
    }

}
