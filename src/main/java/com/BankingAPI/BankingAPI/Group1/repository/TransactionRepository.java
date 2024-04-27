package com.BankingAPI.BankingAPI.Group1.repository;

import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction>{
    List<Transaction> findByUserId(Long userId);

}

