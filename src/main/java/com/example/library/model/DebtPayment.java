package com.example.library.model;

import java.sql.Date;
import java.sql.Time;

public class DebtPayment {
    private int paymentId;
    private String customerId;
    private String customerName;
    private double amountPaid;
    private Date paymentDate;
    private Time paymentTime;
    private String status;

    public DebtPayment(int paymentId, String customerId, String customerName, double amountPaid,
                       Date paymentDate, Time paymentTime, String status) {
        this.paymentId = paymentId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.paymentTime = paymentTime;
        this.status = status;
    }

    public int getPaymentId() { return paymentId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public double getAmountPaid() { return amountPaid; }
    public Date getPaymentDate() { return paymentDate; }
    public Time getPaymentTime() { return paymentTime; }
    public String getStatus() { return status; }
}
