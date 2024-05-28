package com.BankingAPI.BankingAPI.Group1.service;

import com.BankingAPI.BankingAPI.Group1.config.BeanFactory;
import com.BankingAPI.BankingAPI.Group1.exception.CustomAuthenticationException;
import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import com.BankingAPI.BankingAPI.Group1.model.dto.*;
import com.BankingAPI.BankingAPI.Group1.repository.AccountRepository;
import com.BankingAPI.BankingAPI.Group1.repository.UserRepository;
import com.BankingAPI.BankingAPI.Group1.util.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;
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

    public List<UserGETResponseDTO> getAllUsers() throws Exception {
        try {
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
                            user.isActive(),
                            user.getUserType()))
                    .collect(Collectors.toList());
        } catch( Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public  Users findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public  Users findByEmail(String email) {
        return userRepository.findMemberByEmail(email).orElse(null);
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
                true,
                UserType.ROLE_CUSTOMER,
                bCryptPasswordEncoder.encode(userDTO.password())
        );
        return userRepository.save(newUser);
    }

    public boolean checkAndUpdateDailyLimit(Users user, double amount) {
        double updatedAmount = user.getDailyLimit() - amount;
        if (updatedAmount < 0) {
            return false;
        }
        user.setDailyLimit(updatedAmount);
        return true;
    }


    public boolean emailExists(String email) {
        return userRepository.findMemberByEmail(email).isPresent();
    }

    public FindIbanResponseDTO getIbanByFirstNameLastName(FindIbanRequestDTO request) {
        try {
            beanFactory.validateAuthentication();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return accountRepository.findIbanByNames(request.firstName(), request.lastName())
                .map(FindIbanResponseDTO::new)
                .orElse(null);
    }


    public String login(String username, String password) throws CustomAuthenticationException {
        Users user = this.userRepository.findMemberByUsername(username)
                .orElseThrow(() -> new CustomAuthenticationException("User not found"));
        if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return jwtTokenProvider.createToken(user.getId(), user.getUserType(), user.isApproved());
        } else {
            throw new CustomAuthenticationException("Invalid username/password");
        }
    }
    public String atmLogin(String email, String password) throws CustomAuthenticationException {
        Users user = this.userRepository.findMemberByEmail(email)
                .orElseThrow(() -> new CustomAuthenticationException("User not found"));
        if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return jwtTokenProvider.createToken(user.getId(), user.getUserType(), user.isApproved());
        } else {
            throw new CustomAuthenticationException("Invalid email/password");
        }
    }


    public List<UserGETResponseDTO> getUnapprovedUsers() throws RuntimeException{
        try{
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
                            user.isActive(),
                            user.getUserType()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get unapproved users: " + e.getMessage());
        }
    }

    public List<AccountDetailsGETResponse> getAccountDetailsForCurrentUser() {
        Users currentUser = beanFactory.getCurrentUser();
        List<Account> accounts = accountRepository.findAccountsByUserId(currentUser.getId());

        if (accounts.isEmpty()) {
            return Collections.emptyList();
        }

        return accounts.stream().map(account -> new AccountDetailsGETResponse(
                currentUser.getUsername(),
                currentUser.getEmail(),
                currentUser.getFirstName(),
                currentUser.getLastName(),
                currentUser.getBSN(),
                currentUser.getPhoneNumber(),
                currentUser.getBirthDate(),
                account.getIBAN(),
                account.getCurrency(),
                account.getAccountType(),
                account.getBalance(),
                account.getAbsoluteLimit()
        )).collect(Collectors.toList());
    }


    public void approveUser(long userId, UserApprovalDTO approvalDTO) throws EntityNotFoundException{
        try {
            Users currentUser = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            currentUser.setApproved(true);
            currentUser.setDailyLimit(approvalDTO.dailyLimit());
            accountService.createAccountsForUser(currentUser, approvalDTO);
            userRepository.save(currentUser);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to approve user: " + e.getMessage(), e);
        }
    }

    public void updateDailyLimit(Users user) throws EntityNotFoundException {
        Users currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + user.getId()));

        currentUser.setDailyLimit(user.getDailyLimit());
        userRepository.save(currentUser);
    }


    public UserGetOneRESPONSE getUserDetails() {
        Users currentUser = beanFactory.getCurrentUser();
        return new UserGetOneRESPONSE(currentUser.getFirstName(), currentUser.getLastName());
    }

    public void closeAccount(long userId) throws Exception {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        if(user.getUserType() == UserType.ROLE_CUSTOMER && user.isActive()) {
            user.setActive(false);
            accountService.closeAccounts(userId);
            userRepository.save(user);
        } else {
            throw new Exception("This account can't be closed.");
        }
    }
}
