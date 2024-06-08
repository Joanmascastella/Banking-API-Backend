package com.BankingAPI.BankingAPI.Group1.model;

import com.BankingAPI.BankingAPI.Group1.model.dto.TransactionGETPOSTResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


class TransactionTest {

    @Test
    void newTransactionShouldNotBeNull(){
        Transaction transaction = new Transaction();
        assertNotNull(transaction);
    }

    @Test
    void newTransactionShouldNotBeEmpty(){
        Transaction transaction = new Transaction();
        assertNotNull(transaction);
    }

    @Test
    void fromAccountShouldBeDifferentThanToAccount() {
        Users user = new Users();
        Transaction transaction = new Transaction(user, "Account1", "Account2", 100.0, LocalDate.now());
        assertTrue(transaction.isFromAccountDifferentThanToAccount());
    }

    @Test
    void fromAccountShouldNotBeEqualToToAccount() {
        Users user = new Users();
        Transaction transaction = new Transaction(user, "Account1", "Account1", 100.0, LocalDate.now());
        assertFalse(transaction.isFromAccountDifferentThanToAccount());
    }

    @Test
    void newTransactionShouldNotBeNegative() {
        Users user = new Users();
        Transaction transaction = new Transaction(user, "Account1", "Account2", -100.0, LocalDate.now());
        assertTrue(transaction.isAmountNegative());
    }

    @Test
    void newTransactionShouldBePositive() {
        Users user = new Users();
        Transaction transaction = new Transaction(user, "Account1", "Account2", 100.0, LocalDate.now());
        assertTrue(transaction.isAmountPositive());
    }

    @Test
    void transactionShouldHaveValidUser() {
        Users user = new Users();
        Transaction transaction = new Transaction(user, "Account1", "Account2", 100.0, LocalDate.now());
        assertNotNull(transaction.getUser());
    }


    @Test
    void transactionGettersAndSettersShouldWork() {
        Users user = new Users();
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setFromAccount("Account1");
        transaction.setToAccount("Account2");
        transaction.setAmount(100.0);
        transaction.setDate(LocalDate.now());

        assertEquals(user, transaction.getUser());
        assertEquals("Account1", transaction.getFromAccount());
        assertEquals("Account2", transaction.getToAccount());
        assertEquals(100.0, transaction.getAmount());
        assertEquals(LocalDate.now(), transaction.getDate());
    }

    //This unit test is based on the guide at https://www.baeldung.com/entity-to-and-from-dto-for-a-java-spring-application

    @Test
    public void whenConvertTransactionEntityToTransactionDTO_thenCorrect() {

        ObjectMapper mapper = JsonMapperFactory.createObjectMapper();

        Users user = new Users();
        Transaction transaction =  new Transaction(user, "Account1", "Account2", 100d, LocalDate.now());

        TransactionGETPOSTResponseDTO transactionDTO = mapper.convertValue(transaction, TransactionGETPOSTResponseDTO.class);
        assertEquals(transaction.getId(), transactionDTO.id());
        assertEquals(transaction.getFromAccount(), transactionDTO.fromAccount());
        assertEquals(transaction.getToAccount(), transactionDTO.toAccount());
        assertEquals(transaction.getToAccount(), transactionDTO.toAccount());
        assertEquals(transaction.getDate(), transactionDTO.date());


    }
}
