package com.upi.upi_payment_system.dto;

public class SendMoneyRequest {

    private String senderUpi;
    private String receiverUpi;
    private Double amount;
    private String upiPin;

    public String getSenderUpi() {
        return senderUpi;
    }

    public String getReceiverUpi() {
        return receiverUpi;
    }

    public Double getAmount() {
        return amount;
    }

    public String getUpiPin() {
        return upiPin;
    }
}
