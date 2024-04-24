package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.model.User;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserDTO;
import com.BankingAPI.BankingAPI.Group1.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(UserDTO userDTO) {
        User newUser = new User(
                userDTO.username(),
                userDTO.email(),
                userDTO.firstName(),
                userDTO.lastName(),
                userDTO.BSN(),
                userDTO.phoneNumber(),
                userDTO.birthDate(),
                userDTO.totalBalance(),
                userDTO.dailyLimit(),
                userDTO.isApproved(),
                userDTO.userType(),
                userDTO.password()
        );
        return userRepository.save(newUser);
    }
}
