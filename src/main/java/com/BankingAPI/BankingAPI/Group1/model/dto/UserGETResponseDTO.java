package com.BankingAPI.BankingAPI.Group1.model.dto;

import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;

import java.time.LocalDate;

public record UserGETResponseDTO(long id, String username, String email, String firstName, String lastName, String BSN, String phoneNumber, LocalDate birthDate, double totalBalance, Double dailyLimit, boolean isApproved, boolean isActive, UserType type) {
}
