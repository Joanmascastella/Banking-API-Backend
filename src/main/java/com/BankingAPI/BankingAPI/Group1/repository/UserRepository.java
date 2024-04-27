package com.BankingAPI.BankingAPI.Group1.repository;

import com.BankingAPI.BankingAPI.Group1.model.Users;
import io.micrometer.observation.ObservationFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    @Query("SELECT u FROM Users u WHERE u.email = ?1")
    Optional<Users> findByEmail(String email);

    Optional<Users> findMemberByUsername(String name);




}
