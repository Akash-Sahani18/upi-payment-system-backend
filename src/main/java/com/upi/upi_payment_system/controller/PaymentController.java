package com.upi.upi_payment_system.controller;

import com.upi.upi_payment_system.model.Transaction;
import com.upi.upi_payment_system.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    //  SEND MONEY
    @PostMapping("/send")
    public String sendMoney(
            @RequestParam String senderUpi,
            @RequestParam String receiverUpi,
            @RequestParam Double amount,
            @RequestParam String upiPin
    ) {
        return paymentService.sendMoney(senderUpi, receiverUpi, amount, upiPin);
    }

    //  TRANSACTION HISTORY
    @GetMapping("/history/{upiId}")
    public Page<Transaction> getTransactionHistory(
            @PathVariable String upiId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return paymentService.getTransactionHistory(
                upiId,
                PageRequest.of(page, size)
        );
    }
}

