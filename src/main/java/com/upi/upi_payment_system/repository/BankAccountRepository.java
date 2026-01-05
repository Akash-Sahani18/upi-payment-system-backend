package com.upi.upi_payment_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.upi.upi_payment_system.model.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByUpiId(String upiId);
}
