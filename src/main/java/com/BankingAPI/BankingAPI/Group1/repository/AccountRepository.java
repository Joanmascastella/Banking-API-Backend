package com.BankingAPI.BankingAPI.Group1.repository;

import com.BankingAPI.BankingAPI.Group1.model.Account;
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

    boolean existsByIBAN(String iban);

    //Find all savings accounts of a user which are linked to its user id
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.accountType LIKE 'SAVINGS'")
    List<Account> findSavingsAccountsByUserId(@Param("userId") long userId);

    //Find all checking accounts of a user which are linked to its user id
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.accountType LIKE 'CHECKING'")
    List<Account> findCheckingAccountsByUserId(@Param("userId") long userId);

    //Find all accounts with an absolute limit less than or equal to a specific limit
    @Query("SELECT a FROM Account a  WHERE a.absoluteLimit <= :absoluteLimit")
    List<Account> findByAbsoluteLimit(@Param("absoluteLimit") double absoluteLimit);

    //Find all accounts which were set as inactive by the bank
    @Query("SELECT a FROM Account a  WHERE a.isActive = FALSE")
    List<Account> findByInactiveTag();

    @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
    List<Account> findAccountsByUserId(@Param("userId") long userId);
}
