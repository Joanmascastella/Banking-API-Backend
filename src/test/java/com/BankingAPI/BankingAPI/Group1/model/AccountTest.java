package com.BankingAPI.BankingAPI.Group1.model;

import com.BankingAPI.BankingAPI.Group1.model.Enums.AccountType;
import com.BankingAPI.BankingAPI.Group1.model.dto.AccountGETPOSTResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void newAccountShouldNotBeNull(){
        Account account = new Account();
        assertNotNull(account);
    }

    @Test
    void newAccountShouldHaveCorrectValues() {
        Users user = new Users();
        Account account = new Account(user, "NL91ABNA0417164300", "EUR", AccountType.CHECKING, true, 1000.0, 0.0);

        assertEquals(user, account.getUser());
        assertEquals("NL91ABNA0417164300", account.getIBAN());
        assertEquals("EUR", account.getCurrency());
        assertEquals(AccountType.CHECKING, account.getAccountType());
        assertTrue(account.isActive());
        assertEquals(1000.0, account.getBalance());
        assertEquals(0.0, account.getAbsoluteLimit());
    }

    @Test
    void testGettersAndSetters() {
        Users user = new Users();
        Account account = new Account();

        account.setUser(user);
        account.setIBAN("NL91ABNA0417164300");
        account.setCurrency("USD");
        account.setAccountType(AccountType.SAVINGS);
        account.setActive(false);
        account.setBalance(2000.0);
        account.setAbsoluteLimit(500.0);

        assertEquals(user, account.getUser());
        assertEquals("NL91ABNA0417164300", account.getIBAN());
        assertEquals("USD", account.getCurrency());
        assertEquals(AccountType.SAVINGS, account.getAccountType());
        assertFalse(account.isActive());
        assertEquals(2000.0, account.getBalance());
        assertEquals(500.0, account.getAbsoluteLimit());
    }

    @Test
    void accountBalanceShouldNotBeNegative() {
        Account account = new Account();
        account.setBalance(100.0);
        assertTrue(account.getBalance() >= 0);
    }

    @Test
    void accountAbsoluteLimitShouldNotBeNegative() {
        Account account = new Account();
        account.setAbsoluteLimit(50.0);
        assertTrue(account.getAbsoluteLimit() >= 0);
    }

    @Test
    void accountIBANShouldBeValid() {
        Account account = new Account();
        account.setIBAN("NL91ABNA0417164300");
        assertNotNull(account.getIBAN());
        assertFalse(account.getIBAN().isEmpty());
    }

    @Test
    void accountCurrencyShouldBeValid() {
        Account account = new Account();
        account.setCurrency("EUR");
        assertNotNull(account.getCurrency());
        assertFalse(account.getCurrency().isEmpty());
    }

    @Test
    void accountShouldHaveValidUser() {
        Account account = new Account();
        Users user = new Users();
        account.setUser(user);
        assertNotNull(account.getUser());
    }

    @Test
    void accountShouldHaveValidAccountType() {
        Account account = new Account();
        account.setAccountType(AccountType.SAVINGS);
        assertNotNull(account.getAccountType());
    }

    @Test
    public void whenConvertAccountEntityToAccountDTO_thenCorrect() {

        ObjectMapper mapper = JsonMapperFactory.createObjectMapper();

        Users user = new Users();
        Account account = new Account(user, "NL89INHO0044053200", "EUR", AccountType.CHECKING, true, 5000.0, 0.00);

        AccountGETPOSTResponseDTO accountDTO = mapper.convertValue(account, AccountGETPOSTResponseDTO.class);
        assertEquals(account.getId(), accountDTO.id());
        assertEquals(account.getIBAN(), accountDTO.IBAN());
        assertEquals(account.getCurrency(), accountDTO.currency());
        assertEquals(account.getAccountType(), accountDTO.accountType());
        assertEquals(account.isActive(), accountDTO.isActive());
        assertEquals(account.getBalance(), accountDTO.balance());
        assertEquals(account.getAbsoluteLimit(), accountDTO.absoluteLimit());
    }
}
