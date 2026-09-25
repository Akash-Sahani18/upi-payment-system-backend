package com.upi.upi_payment_system;

import com.upi.upi_payment_system.model.BankAccount;
import com.upi.upi_payment_system.repository.BankAccountRepository;
import com.upi.upi_payment_system.repository.TransactionRepository;
import com.upi.upi_payment_system.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class UpiPaymentSystemApplicationTests {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldHandle200ConcurrentRequestsWithoutDuplicateTransactions() throws Exception {
        BankAccount sender = new BankAccount();
        sender.setUpiId("sender@upi");
        sender.setAccountNumber("10001");
        sender.setBankName("Test Bank");
        sender.setBalance(100.0);
        sender.setUpiPinHash(passwordEncoder.encode("1234"));

        BankAccount receiver = new BankAccount();
        receiver.setUpiId("receiver@upi");
        receiver.setAccountNumber("10002");
        receiver.setBankName("Test Bank");
        receiver.setBalance(0.0);
        receiver.setUpiPinHash(passwordEncoder.encode("1234"));

        bankAccountRepository.save(sender);
        bankAccountRepository.save(receiver);

        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            final String key = "concurrent-payment-" + i;
            for (int attempt = 0; attempt < 2; attempt++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return paymentService.sendMoney(
                            "sender@upi",
                            "receiver@upi",
                            1.0,
                            "1234",
                            key
                    );
                }));
            }
        }

        start.countDown();

        int successfulResponses = 0;
        for (Future<String> future : futures) {
            String result = future.get(30, TimeUnit.SECONDS);
            if ("Payment Successful".equals(result)) {
                successfulResponses++;
            }
        }

        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        List<BankAccount> accounts = bankAccountRepository.findAll();

        BankAccount finalSender = accounts.stream()
                .filter(account -> "sender@upi".equals(account.getUpiId()))
                .findFirst()
                .orElseThrow();

        BankAccount finalReceiver = accounts.stream()
                .filter(account -> "receiver@upi".equals(account.getUpiId()))
                .findFirst()
                .orElseThrow();

        assertEquals(200, futures.size());
        assertEquals(200, successfulResponses);
        assertEquals(0.0, finalSender.getBalance(), 0.001);
        assertEquals(100.0, finalReceiver.getBalance(), 0.001);
        assertEquals(100, transactionRepository.count());
    }
}
