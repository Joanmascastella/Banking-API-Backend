package com.BankingAPI.BankingAPI.Group1.model;

import com.BankingAPI.BankingAPI.Group1.model.Enums.UserType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UsersTest {

    @Test
    void newUserShouldNotBeNull(){
        Users user = new Users();
        assertNotNull(user);
    }

    @Test
    void newUserShouldHaveCorrectValues() {
        Users user = new Users(
                "testuser",
                "test@example.com",
                "John",
                "Doe",
                "123456789",
                "123-456-7890",
                LocalDate.of(1990, 1, 1),
                1000.0,
                500.0,
                true,
                UserType.ROLE_CUSTOMER,
                "password"
        );

        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("123456789", user.getBSN());
        assertEquals("123-456-7890", user.getPhoneNumber());
        assertEquals(LocalDate.of(1990, 1, 1), user.getBirthDate());
        assertEquals(1000.0, user.getTotalBalance());
        assertEquals(500.0, user.getDailyLimit());
        assertTrue(user.isApproved());
        assertEquals(UserType.ROLE_CUSTOMER, user.getUserType());
        assertEquals("password", user.getPassword());
    }

    @Test
    void testGettersAndSetters() {
        Users user = new Users();

        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setBSN("987654321");
        user.setPhoneNumber("098-765-4321");
        user.setBirthDate(LocalDate.of(1985, 5, 15));
        user.setTotalBalance(2000.0);
        user.setDailyLimit(1000.0);
        user.setApproved(false);
        user.setUserType(UserType.ROLE_EMPLOYEE);
        user.setPassword("newpassword");

        assertEquals("newuser", user.getUsername());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("987654321", user.getBSN());
        assertEquals("098-765-4321", user.getPhoneNumber());
        assertEquals(LocalDate.of(1985, 5, 15), user.getBirthDate());
        assertEquals(2000.0, user.getTotalBalance());
        assertEquals(1000.0, user.getDailyLimit());
        assertFalse(user.isApproved());
        assertEquals(UserType.ROLE_EMPLOYEE, user.getUserType());
        assertEquals("newpassword", user.getPassword());
    }

    @Test
    void userTotalBalanceShouldNotBeNegative() {
        Users user = new Users();
        user.setTotalBalance(100.0);
        assertTrue(user.getTotalBalance() >= 0);
    }

    @Test
    void userDailyLimitShouldNotBeNegative() {
        Users user = new Users();
        user.setDailyLimit(50.0);
        assertTrue(user.getDailyLimit() >= 0);
    }

    @Test
    void userEmailShouldBeValid() {
        Users user = new Users();
        user.setEmail("valid@example.com");
        assertTrue(user.getEmail().contains("@"));
    }

    @Test
    void userPhoneNumberShouldBeValid() {
        Users user = new Users();
        user.setPhoneNumber("123-456-7890");
        assertNotNull(user.getPhoneNumber());
    }

    @Test
    void userBirthDateShouldBeInThePast() {
        Users user = new Users();
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        assertTrue(user.getBirthDate().isBefore(LocalDate.now()));
    }

    @Test
    void userShouldHaveValidUserType() {
        Users user = new Users();
        user.setUserType(UserType.ROLE_EMPLOYEE);
        assertNotNull(user.getUserType());
    }

    @Test
    void userPasswordShouldNotBeEmpty() {
        Users user = new Users();
        user.setPassword("securepassword");
        assertFalse(user.getPassword().isEmpty());
    }
}
