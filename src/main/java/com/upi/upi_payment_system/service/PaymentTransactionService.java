package com.upi.upi_payment_system.service;

import com.upi.upi_payment_system.model.BankAccount;
import com.upi.upi_payment_system.model.Transaction;
import com.upi.upi_payment_system.model.TransactionStatus;
import com.upi.upi_payment_system.repository.BankAccountRepository;
import com.upi.upi_payment_system.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String processPayment(
            String senderUpi,
            String receiverUpi,
            Double amount,
            String upiPin,
            String idempotencyKey
    ) {
        BankAccount sender = bankAccountRepository.findByUpiId(senderUpi)
                .orElseThrow(() -> new RuntimeException("Sender UPI not found"));

        BankAccount receiver = bankAccountRepository.findByUpiId(receiverUpi)
                .orElseThrow(() -> new RuntimeException("Receiver UPI not found"));

        if (!passwordEncoder.matches(upiPin, sender.getUpiPinHash())) {
            saveTransaction(senderUpi, receiverUpi, amount,
                    TransactionStatus.FAILED, idempotencyKey);
            throw new RuntimeException("Invalid UPI PIN");
        }

        if (sender.getBalance() < amount) {
            saveTransaction(senderUpi, receiverUpi, amount,
                    TransactionStatus.FAILED, idempotencyKey);
            throw new RuntimeException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        bankAccountRepository.save(sender);
        bankAccountRepository.save(receiver);

        saveTransaction(senderUpi, receiverUpi, amount,
                TransactionStatus.SUCCESS, idempotencyKey);

        return "Payment Successful";
    }

    private void saveTransaction(
            String sender,
            String receiver,
            Double amount,
            TransactionStatus status,
            String idempotencyKey
    ) {
        Transaction tx = Transaction.builder()
                .senderUpiId(sender)
                .receiverUpiId(receiver)
                .amount(amount)
                .status(status)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(tx);
    }
}
