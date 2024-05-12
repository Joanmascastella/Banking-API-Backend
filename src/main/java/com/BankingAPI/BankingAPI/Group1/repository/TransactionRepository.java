package com.BankingAPI.BankingAPI.Group1.repository;

import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    // Find transactions by user ID by directly accessing the 'user' association
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    //Find all transactions initialized by the customers
    @Query("SELECT t FROM Transaction t WHERE t.user.userType = ROLE_CUSTOMER ")
    List<Transaction> findByUserTypeCustomer();

    //Find all transactions initialized by the employees
    @Query("SELECT t FROM Transaction t WHERE t.user.userType = 'ROLE_EMPLOYEE'")
    List<Transaction> findByUserTypeEmployee();

    //Find all ATM transactions
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount LIKE 'ATM' OR t.toAccount LIKE 'ATM'")
    List<Transaction> findATMTransactions();

    //Find all ATM transactions for a specific user by user ID
    //Refactored query to use the 'user' field of Transaction table instead of the field userId after refactoring the Transaction model
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount LIKE 'ATM' OR t.toAccount LIKE 'ATM' AND t.user.id = :userId")
    List<Transaction> findATMTransactionsByUserId(@Param("userId") long userId);

    //Find ATM withdrawals for a specific user by user ID
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount LIKE 'ATM' AND t.user.id = :userId")
    List<Transaction> findATMWithdrawalsByUserId(@Param("userId") long userId);

    //Find ATM deposits for a specific user by user id
    @Query("SELECT t FROM Transaction t WHERE t.toAccount LIKE 'ATM' AND t.user.id = :userId")
    List<Transaction> findATMDepositsByUserId(@Param("userId") long userId);

    //Find all transactions made online
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount NOT LIKE 'ATM' AND t.toAccount NOT LIKE 'ATM'")
    List<Transaction> findOnlineTransactions();

    //Find all transactions made online by a customer
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount NOT LIKE 'ATM' AND t.toAccount NOT LIKE 'ATM' AND t.user.userType = 'ROLE_CUSTOMER'")
    List<Transaction> findOnlineTransactionsByCustomers();

    //Find all transactions made online by an employee
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount NOT LIKE 'ATM' AND t.toAccount NOT LIKE 'ATM' AND t.user.userType = 'ROLE_EMPLOYEE'")
    List<Transaction> findOnlineTransactionsByEmployees();

    //Find online transactions by user id
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount NOT LIKE 'ATM' AND t.toAccount NOT LIKE 'ATM' AND t.user.id = :userId")
    List<Transaction> findOnlineTransactionsByUserId(@Param("userId") long userId);





}
