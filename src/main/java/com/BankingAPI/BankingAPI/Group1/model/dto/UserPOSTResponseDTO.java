package com.BankingAPI.BankingAPI.Group1.model.dto;

import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;

import java.time.LocalDate;
import java.util.List;

public record UserPOSTResponseDTO(String username, String email, String firstName, String lastName, String BSN, String phoneNumber, LocalDate birthDate, String password) {
}
