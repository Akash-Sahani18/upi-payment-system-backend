package com.upi.upi_payment_system.repository;

import com.upi.upi_payment_system.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface BankAccountRepository
        extends JpaRepository<BankAccount, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BankAccount> findByUpiId(String upiId);

    Optional<BankAccount> findFirstByUpiId(String upiId);
}