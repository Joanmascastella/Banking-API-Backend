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

    @Query("SELECT a.IBAN FROM Account a JOIN a.user u WHERE u.firstName = :firstName AND u.lastName = :lastName AND a.accountType LIKE 'CHECKING'")
    Optional<String> findIbanByNames(String firstName, String lastName);
    boolean existsByIBAN(String iban);

    //Find all accounts with an absolute limit less than or equal to a specific limit
    @Query("SELECT a FROM Account a  WHERE a.absoluteLimit <= :absoluteLimit")
    List<Account> findByAbsoluteLimit(@Param("absoluteLimit") double absoluteLimit);


    @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
    List<Account> findAccountsByUserId(@Param("userId") long userId);
}
