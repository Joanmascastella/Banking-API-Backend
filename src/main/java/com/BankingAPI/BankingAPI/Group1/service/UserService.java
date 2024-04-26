package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserGETResponseDTO;
import com.BankingAPI.BankingAPI.Group1.repository.UserRepository;
import com.BankingAPI.BankingAPI.Group1.util.JwtTokenProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;


    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public List<UserGETResponseDTO> getAllUsers() {
        List<Users> users = userRepository.findAll();
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


    public Users createUser(UserPOSTResponseDTO userPOSTResponseDTO) {
        if (emailExists(userPOSTResponseDTO.email())) {
            throw new IllegalStateException("Email already in use");
        }
        Users newUsers = new Users(
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
        return userRepository.save(newUsers);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    //I wrote this to test if the jwt was working
    //    public String login(String username, String password) throws Exception {
    //        Users user = this.userRepository
    //                .findMemberByUsername(username)
    //                .orElseThrow(() -> new AuthenticationException("User not found"));
    //        if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
    //            return jwtTokenProvider.createToken(user.getUsername(), user.getId(), user.getUserType());
    //        } else {
    //            throw new AuthenticationException("Invalid username/password");
    //        }
    //    }
}
