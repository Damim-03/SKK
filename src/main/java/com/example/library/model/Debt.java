package com.example.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Debt {
    private String customerId;
    private String customerName;
    private BigDecimal totalDebt;
    private LocalDate debtDate;
    private String notes;

    public Debt(String customerId, String customerName, BigDecimal totalDebt, LocalDate debtDate, String notes) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalDebt = totalDebt;
        this.debtDate = debtDate;
        this.notes = notes;
    }

    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public BigDecimal getTotalDebt() { return totalDebt; }
    public LocalDate getDebtDate() { return debtDate; }
    public String getNotes() { return notes; }
}
