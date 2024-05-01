package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.config.BeanFactory;
import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.FindIbanResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserPOSTResponseDTO;
import com.BankingAPI.BankingAPI.Group1.model.dto.UserGETResponseDTO;
import com.BankingAPI.BankingAPI.Group1.repository.AccountRepository;
import com.BankingAPI.BankingAPI.Group1.repository.UserRepository;
import com.BankingAPI.BankingAPI.Group1.util.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final AccountRepository accountRepository;
    private UserRepository userRepository;
    private AccountService accountService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final BeanFactory beanFactory;


    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtTokenProvider jwtTokenProvider, AccountRepository accountRepository, BeanFactory beanFactory, AccountService accountService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.accountRepository = accountRepository;
        this.beanFactory = beanFactory;
    }

    public List<UserGETResponseDTO> getAllUsers() {
        try {
            beanFactory.validateAuthentication();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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


    public Users createUser(UserPOSTResponseDTO userDTO) {
        if (emailExists(userDTO.email())) {
            throw new IllegalStateException("Email already in use");
        }
        Users newUser = new Users(
                userDTO.username(),
                userDTO.email(),
                userDTO.firstName(),
                userDTO.lastName(),
                userDTO.BSN(),
                userDTO.phoneNumber(),
                userDTO.birthDate(),
                0.0,
                0.0,
                false,
                Collections.singletonList(UserType.ROLE_CUSTOMER),
                bCryptPasswordEncoder.encode(userDTO.password())
        );
        return userRepository.save(newUser);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public FindIbanResponseDTO getIbanByFirstNameLastName(String firstName, String lastName) {
        try {
            beanFactory.validateAuthentication();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return accountRepository.findIbanByNames(firstName, lastName)
                .map(iban -> new FindIbanResponseDTO(iban))
                .orElse(null);
    }

    public String login(String username, String password) throws Exception {
        Users user = this.userRepository
                .findMemberByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));
        if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return jwtTokenProvider.createToken(user.getUsername(), user.getId(), user.getUserType());
        } else {
            throw new AuthenticationException("Invalid username/password");
        }
    }
    
    public List<UserGETResponseDTO> getUnapprovedUsers() {
        List<Users> users = userRepository.findByIsApproved(false);
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

    public void approveUser(Users user, double absoluteSavingLimit, double absoluteCheckingLimit) {
        Users currentUser = userRepository.findById(user.getId())
                        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + user.getId()));
        currentUser.setApproved(true);
        currentUser.setDailyLimit(user.getDailyLimit());
        accountService.createAccountsForUser(user, absoluteSavingLimit, absoluteCheckingLimit);
        userRepository.save(currentUser);
    }

    public void updateDailyLimit(Users user) {
        Users currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + user.getId()));
        currentUser.setDailyLimit(user.getDailyLimit());
        userRepository.save(currentUser);
    }
}
