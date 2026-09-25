package com.upi.upi_payment_system;

import com.upi.upi_payment_system.model.BankAccount;
import com.upi.upi_payment_system.repository.BankAccountRepository;
import com.upi.upi_payment_system.repository.TransactionRepository;
import com.upi.upi_payment_system.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UpiPaymentSystemApplicationTests {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final String SENDER_UPI = "sender@upi";
    private static final String RECEIVER_UPI = "receiver@upi";

    @BeforeEach
    void setUp() {

        transactionRepository.deleteAll();
        bankAccountRepository.deleteAll();

        BankAccount sender = new BankAccount();
        sender.setUpiId(SENDER_UPI);
        sender.setAccountNumber("111111111111");
        sender.setBankName("Test Bank");
        sender.setBalance(100.0);
        sender.setUpiPinHash(
                "$2a$10$kOYIGJpP8bGtBBGNhVQvT.MIsa/U4sVZvd0RFLWQHZea8uLVQHmoi"
        );

        BankAccount receiver = new BankAccount();
        receiver.setUpiId(RECEIVER_UPI);
        receiver.setAccountNumber("222222222222");
        receiver.setBankName("Test Bank");
        receiver.setBalance(0.0);
        receiver.setUpiPinHash(
                "$2a$10$kOYIGJpP8bGtBBGNhVQvT.MIsa/U4sVZvd0RFLWQHZea8uLVQHmoi"
        );

        bankAccountRepository.save(sender);
        bankAccountRepository.save(receiver);
    }

    /**
     * Tests idempotency under concurrent duplicate requests.
     *
     * 100 unique idempotency keys are each submitted twice,
     * producing 200 concurrent requests.
     *
     * Expected:
     * - 200 requests complete successfully
     * - only 100 transactions are created
     * - sender loses exactly 100
     * - receiver receives exactly 100
     */
    @Test
    void shouldHandle200ConcurrentRequestsWithoutDuplicateTransactions()
            throws InterruptedException, ExecutionException {

        int uniquePayments = 100;
        int totalRequests = uniquePayments * 2;
        int threadCount = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = 0; i < uniquePayments; i++) {

            String idempotencyKey =
                    "concurrent-payment-" + i;

            /*
             * Submit the same payment twice.
             */
            for (int j = 0; j < 2; j++) {

                tasks.add(() -> {

                    startLatch.await();

                    return paymentService.sendMoney(
                            SENDER_UPI,
                            RECEIVER_UPI,
                            1.0,
                            "1234",
                            idempotencyKey
                    );
                });
            }
        }

        List<Future<String>> futures = new ArrayList<>();

        for (Callable<String> task : tasks) {
            futures.add(executor.submit(task));
        }

        /*
         * Release all worker threads at approximately the same time.
         */
        startLatch.countDown();

        int successfulRequests = 0;

        for (Future<String> future : futures) {

            String result = future.get();

            assertNotNull(result);

            assertTrue(
                    result.equals("Payment Successful")
                            || result.equals("Payment Failed"),
                    "Unexpected payment result: " + result
            );

            successfulRequests++;
        }

        executor.shutdown();

        assertTrue(
                executor.awaitTermination(30, TimeUnit.SECONDS),
                "Executor did not terminate within 30 seconds"
        );

        assertEquals(
                totalRequests,
                successfulRequests,
                "All 200 requests should complete"
        );

        /*
         * Use the non-locking read method for verification.
         */
        BankAccount sender =
                bankAccountRepository.findFirstByUpiId(SENDER_UPI)
                        .orElseThrow();

        BankAccount receiver =
                bankAccountRepository.findFirstByUpiId(RECEIVER_UPI)
                        .orElseThrow();

        long transactionCount =
                transactionRepository.count();

        assertEquals(
                0.0,
                sender.getBalance(),
                0.001,
                "Sender balance should be 0"
        );

        assertEquals(
                100.0,
                receiver.getBalance(),
                0.001,
                "Receiver balance should be 100"
        );

        assertEquals(
                100,
                transactionCount,
                "Only 100 unique transactions should be created"
        );
    }

    /**
     * Tests concurrent updates against the SAME sender account.
     *
     * 100 different payments of 1.0 are executed concurrently.
     *
     * This specifically exercises the pessimistic row-level lock
     * on the sender account.
     *
     * Expected:
     * - all 100 payments succeed
     * - sender balance becomes exactly 0
     * - receiver balance becomes exactly 100
     * - exactly 100 transactions exist
     */
    @Test
    void shouldMaintainCorrectBalanceWithConcurrentTransfers()
            throws InterruptedException, ExecutionException {

        int paymentCount = 100;
        int threadCount = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = 0; i < paymentCount; i++) {

            String idempotencyKey =
                    "row-lock-payment-" + i;

            tasks.add(() -> {

                startLatch.await();

                return paymentService.sendMoney(
                        SENDER_UPI,
                        RECEIVER_UPI,
                        1.0,
                        "1234",
                        idempotencyKey
                );
            });
        }

        List<Future<String>> futures = new ArrayList<>();

        for (Callable<String> task : tasks) {
            futures.add(executor.submit(task));
        }

        /*
         * Start all payment operations concurrently.
         */
        startLatch.countDown();

        int successfulPayments = 0;

        for (Future<String> future : futures) {

            String result = future.get();

            assertEquals(
                    "Payment Successful",
                    result,
                    "Every unique payment should succeed"
            );

            successfulPayments++;
        }

        executor.shutdown();

        assertTrue(
                executor.awaitTermination(30, TimeUnit.SECONDS),
                "Executor did not terminate within 30 seconds"
        );

        BankAccount sender =
                bankAccountRepository.findFirstByUpiId(SENDER_UPI)
                        .orElseThrow();

        BankAccount receiver =
                bankAccountRepository.findFirstByUpiId(RECEIVER_UPI)
                        .orElseThrow();

        long transactionCount =
                transactionRepository.count();

        assertEquals(
                paymentCount,
                successfulPayments,
                "All 100 concurrent payments should succeed"
        );

        assertEquals(
                0.0,
                sender.getBalance(),
                0.001,
                "Sender balance should be exactly 0"
        );

        assertEquals(
                100.0,
                receiver.getBalance(),
                0.001,
                "Receiver should receive all 100 payments"
        );

        assertEquals(
                paymentCount,
                transactionCount,
                "Exactly 100 transactions should be created"
        );
    }
}