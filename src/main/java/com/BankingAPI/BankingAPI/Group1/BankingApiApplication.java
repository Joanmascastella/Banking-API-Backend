package com.BankingAPI.BankingAPI.Group1;

import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.User;
import com.BankingAPI.BankingAPI.Group1.model.UserType;
import com.BankingAPI.BankingAPI.Group1.repository.TransactionRepository;
import com.BankingAPI.BankingAPI.Group1.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class BankingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingApiApplication.class, args);
	}

	@Bean
	public ApplicationRunner applicationRunner(UserRepository userRepository, TransactionRepository transactionRepository) {
		return args -> {
			User newUser = new User("johndoe", "john.doe@example.com", "John", "Doe", "123456789", "0123456789", LocalDate.of(1990, 1, 1), 5000.0, 1000.0, true, UserType.CUSTOMER, "password123");
			userRepository.save(newUser);

			Transaction newTransaction = new Transaction(newUser, "123456789", "123456789", 2000.0, LocalDate.now());
			transactionRepository.save(newTransaction);
		};
	}
}
