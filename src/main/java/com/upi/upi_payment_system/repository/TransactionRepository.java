package com.upi.upi_payment_system.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.upi.upi_payment_system.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findBySenderUpiIdOrReceiverUpiId(
            String senderUpiId,
            String receiverUpiId,
            Pageable pageable
    );

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
