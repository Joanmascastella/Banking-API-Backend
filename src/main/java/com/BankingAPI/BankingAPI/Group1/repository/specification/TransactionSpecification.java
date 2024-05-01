package com.BankingAPI.BankingAPI.Group1.repository.specification;
import com.BankingAPI.BankingAPI.Group1.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;


public class TransactionSpecification {
    public static Specification<Transaction> hasIBAN(String IBAN) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.equal(root.get("fromAccount"), IBAN),
                criteriaBuilder.equal(root.get("toAccount"), IBAN)
        );
    }

    public static Specification<Transaction> amountEquals(double amount) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), amount);
    }

    public static Specification<Transaction> amountGreaterThan(double amount) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("amount"), amount);
    }

    public static Specification<Transaction> amountLessThan(double amount) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("amount"), amount);
    }

    public static Specification<Transaction> isBetweenDates(LocalDate start, LocalDate end) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("date"), start, end);
    }
}
