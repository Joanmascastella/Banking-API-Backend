package com.BankingAPI.BankingAPI.Group1.model.dto;

import com.BankingAPI.BankingAPI.Group1.model.UserType;

import java.time.LocalDate;

public record UserDTO (String username, String email, String firstName, String lastName, String BSN, String phoneNumber, LocalDate birthDate, double totalBalance, double dailyLimit, boolean isApproved, UserType userType, String password) {
}
