package com.BankingAPI.BankingAPI.Group1.repository;

import com.BankingAPI.BankingAPI.Group1.model.Account;
import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;
import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import com.BankingAPI.BankingAPI.Group1.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserId(Long userId);
    Optional<Account> findByIBAN(String iban);

    @Query("SELECT a.IBAN FROM Account a JOIN a.user u WHERE u.firstName = :firstName AND u.lastName = :lastName")
    Optional<String> findIbanByNames(String firstName, String lastName);

    @Query("SELECT a FROM Account a, Users u WHERE a.user.id = u.id AND u.id = :userId AND a.accountType LIKE 'SAVINGS'")
    List<Account> findSavingsAccountsByUserId(@Param("userId") long userId);

    @Query("SELECT a FROM Account a, Users u WHERE a.user.id = u.id AND u.id = :userId AND a.accountType LIKE 'CHECKING'")
    List<Account> findCheckingAccountsByUserId(@Param("userId") long userId);

    @Query("SELECT a FROM Account a  WHERE a.absoluteLimit <= :absoluteLimit")
    List<Account> findByAbsoluteLimit(@Param("absoluteLimit") double absoluteLimit);

    @Query("SELECT a FROM Account a  WHERE a.isActive = FALSE")
    List<Account> findByInactiveTag();



}
