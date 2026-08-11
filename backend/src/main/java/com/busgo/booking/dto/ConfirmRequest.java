package com.busgo.booking.dto;

public class ConfirmRequest {
    private String holdToken; // token returned by hold
    private String paymentReference;

    public String getHoldToken() { return holdToken; }
    public void setHoldToken(String holdToken) { this.holdToken = holdToken; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
}
