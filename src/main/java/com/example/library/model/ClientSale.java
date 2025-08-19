package com.example.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ClientSale {
    private int saleId;
    private LocalDate saleDate;
    private LocalTime saleTime;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal debt;
    private BigDecimal total;
    private String customerName;
    private String customerId;

    public ClientSale(int saleId, LocalDate saleDate, LocalTime saleTime, BigDecimal subtotal,
                      BigDecimal discount, BigDecimal debt, BigDecimal total,
                      String customerName, String customerId) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.saleTime = saleTime;
        this.subtotal = subtotal;
        this.discount = discount;
        this.debt = debt;
        this.total = total;
        this.customerName = customerName;
        this.customerId = customerId;
    }

    public int getSaleId() { return saleId; }
    public LocalDate getSaleDate() { return saleDate; }
    public LocalTime getSaleTime() { return saleTime; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getDebt() { return debt; }
    public BigDecimal getTotal() { return total; }
    public String getCustomerName() { return customerName; }
    public String getCustomerId() { return customerId; }
}
