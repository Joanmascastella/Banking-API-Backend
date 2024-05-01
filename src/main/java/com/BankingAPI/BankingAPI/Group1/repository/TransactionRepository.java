package com.BankingAPI.BankingAPI.Group1.repository;

import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction>{
    List<Transaction> findByUserId(Long userId);
    @Query("SELECT r FROM Transaction r, Users u WHERE r.userId = u.id AND :userType member u.userType")
    List<Transaction> findByUserType(@Param("userType") UserType userType);

    @Query("SELECT r FROM Transaction r WHERE r.fromAccount LIKE '%ATM%'")
    List<Transaction> findATMTransactions();

    @Query("SELECT r FROM Transaction r, Users u WHERE r.userId = u.id AND r.fromAccount LIKE '%ATM%' AND :userId = u.id")
    List<Transaction> findATMTransactionsByUser(@Param("userId") long userId);
}

