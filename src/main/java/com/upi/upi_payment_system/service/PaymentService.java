package com.upi.upi_payment_system.service;

import com.upi.upi_payment_system.model.Transaction;
import com.upi.upi_payment_system.model.TransactionStatus;
import com.upi.upi_payment_system.repository.BankAccountRepository;
import com.upi.upi_payment_system.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaymentTransactionService paymentTransactionService;

    public String sendMoney(
            String senderUpi,
            String receiverUpi,
            Double amount,
            String upiPin,
            String idempotencyKey
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }

        Transaction existing = transactionRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElse(null);

        if (existing != null) {
            return existing.getStatus() == TransactionStatus.SUCCESS
                    ? "Payment Successful"
                    : "Payment Failed";
        }

        try {
            return paymentTransactionService.processPayment(
                    senderUpi,
                    receiverUpi,
                    amount,
                    upiPin,
                    idempotencyKey
            );
        } catch (DataIntegrityViolationException ex) {
            Transaction completed = transactionRepository
                    .findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> ex);

            return completed.getStatus() == TransactionStatus.SUCCESS
                    ? "Payment Successful"
                    : "Payment Failed";
        }
    }

    public Page<Transaction> getTransactionHistory(String upiId, Pageable pageable) {
        return transactionRepository
                .findBySenderUpiIdOrReceiverUpiId(upiId, upiId, pageable);
    }
}
