package com.upi.upi_payment_system.dto;

import lombok.Data;

@Data
public class PaymentRequest {

    private String senderUpi;
    private String receiverUpi;
    private Double amount;
    private String upiPin;
}
