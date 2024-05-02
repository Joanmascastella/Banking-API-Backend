package com.BankingAPI.BankingAPI.Group1.model.dto;

import java.time.LocalDate;

public record UserPOSTResponseDTO(String username, String email, String firstName, String lastName, String BSN, String phoneNumber, LocalDate birthDate, String password) {
}
