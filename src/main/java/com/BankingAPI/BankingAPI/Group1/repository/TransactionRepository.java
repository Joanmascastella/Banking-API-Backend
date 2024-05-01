package com.BankingAPI.BankingAPI.Group1.repository;

import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    // Find transactions by user ID by directly accessing the 'user' association
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    // Updated to join on the 'user' field correctly
    @Query("SELECT t FROM Transaction t WHERE t.user.userType = :userType")
    List<Transaction> findByUserType(@Param("userType") UserType userType);

    // Find transactions where 'fromAccount' contains 'ATM'
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount LIKE '%ATM%'")
    List<Transaction> findATMTransactions();

    // Find ATM transactions for a specific user by user ID
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount LIKE '%ATM%' AND t.user.id = :userId")
    List<Transaction> findATMTransactionsByUser(@Param("userId") long userId);
}
