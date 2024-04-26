package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.model.User;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserGETResponseDTO;
import com.BankingAPI.BankingAPI.Group1.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserGETResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserGETResponseDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getBSN(),
                        user.getPhoneNumber(),
                        user.getBirthDate(),
                        user.getTotalBalance(),
                        user.getDailyLimit(),
                        user.isApproved(),
                        user.getUserType()))
                .collect(Collectors.toList());
    }


    public User createUser(UserPOSTResponseDTO userPOSTResponseDTO) {
        if (emailExists(userPOSTResponseDTO.email())) {
            throw new IllegalStateException("Email already in use");
        }
        User newUser = new User(
                userPOSTResponseDTO.username(),
                userPOSTResponseDTO.email(),
                userPOSTResponseDTO.firstName(),
                userPOSTResponseDTO.lastName(),
                userPOSTResponseDTO.BSN(),
                userPOSTResponseDTO.phoneNumber(),
                userPOSTResponseDTO.birthDate(),
                userPOSTResponseDTO.totalBalance(),
                userPOSTResponseDTO.dailyLimit(),
                userPOSTResponseDTO.isApproved(),
                userPOSTResponseDTO.userType(),
                userPOSTResponseDTO.password()
        );
        return userRepository.save(newUser);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
