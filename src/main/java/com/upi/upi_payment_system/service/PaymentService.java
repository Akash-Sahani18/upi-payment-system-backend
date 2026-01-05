package com.upi.upi_payment_system.service;

import com.upi.upi_payment_system.model.BankAccount;
import com.upi.upi_payment_system.model.Transaction;
import com.upi.upi_payment_system.model.TransactionStatus;
import com.upi.upi_payment_system.repository.BankAccountRepository;
import com.upi.upi_payment_system.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;

    //  SEND MONEY
    @Transactional
    public String sendMoney(
            String senderUpi,
            String receiverUpi,
            Double amount,
            String upiPin
    ) {
        BankAccount sender = bankAccountRepository.findByUpiId(senderUpi)
                .orElseThrow(() -> new RuntimeException("Sender UPI not found"));

        BankAccount receiver = bankAccountRepository.findByUpiId(receiverUpi)
                .orElseThrow(() -> new RuntimeException("Receiver UPI not found"));

        if (!passwordEncoder.matches(upiPin, sender.getUpiPinHash())) {
            saveTransaction(senderUpi, receiverUpi, amount, TransactionStatus.FAILED);
            throw new RuntimeException("Invalid UPI PIN");
        }

        if (sender.getBalance() < amount) {
            saveTransaction(senderUpi, receiverUpi, amount, TransactionStatus.FAILED);
            throw new RuntimeException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        bankAccountRepository.save(sender);
        bankAccountRepository.save(receiver);

        saveTransaction(senderUpi, receiverUpi, amount, TransactionStatus.SUCCESS);

        return "Payment Successful";
    }

    //  TRANSACTION HISTORY
    public Page<Transaction> getTransactionHistory(String upiId, Pageable pageable) {
        return transactionRepository
                .findBySenderUpiIdOrReceiverUpiId(upiId, upiId, pageable);
    }

    private void saveTransaction(
            String sender,
            String receiver,
            Double amount,
            TransactionStatus status
    ) {
        Transaction tx = Transaction.builder()
                .senderUpiId(sender)
                .receiverUpiId(receiver)
                .amount(amount)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(tx);
    }
}
