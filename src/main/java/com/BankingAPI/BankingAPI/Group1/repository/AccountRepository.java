package com.BankingAPI.BankingAPI.Group1.repository;

import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserId(Long userId);
    Optional<Account> findByIBAN(String iban);

    @Query("SELECT a.IBAN FROM Account a JOIN a.user u WHERE u.firstName = :firstName AND u.lastName = :lastName")
    Optional<String> findIbanByNames(String firstName, String lastName);

    boolean existsByIBAN(String iban);
}
