package com.example.library.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class Sale {
    private final IntegerProperty saleId = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> saleDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> saleTime = new SimpleObjectProperty<>();
    private final DoubleProperty subtotal = new SimpleDoubleProperty();
    private final DoubleProperty discount = new SimpleDoubleProperty();
    private final DoubleProperty debt = new SimpleDoubleProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();
    private final StringProperty customerName = new SimpleStringProperty();
    private final StringProperty customerId = new SimpleStringProperty();

    public Sale(int saleId, LocalDate saleDate, LocalTime saleTime, double subtotal,
                double discount, double debt, double total, String customerName, String customerId) {
        this.saleId.set(saleId);
        this.saleDate.set(saleDate);
        this.saleTime.set(saleTime);
        this.subtotal.set(subtotal);
        this.discount.set(discount);
        this.debt.set(debt);
        this.total.set(total);
        this.customerName.set(customerName);
        this.customerId.set(customerId);
    }

    // Property getters
    public IntegerProperty saleIdProperty() { return saleId; }
    public ObjectProperty<LocalDate> saleDateProperty() { return saleDate; }
    public ObjectProperty<LocalTime> saleTimeProperty() { return saleTime; }
    public DoubleProperty subtotalProperty() { return subtotal; }
    public DoubleProperty discountProperty() { return discount; }
    public DoubleProperty debtProperty() { return debt; }
    public DoubleProperty totalProperty() { return total; }
    public StringProperty customerNameProperty() { return customerName; }
    public StringProperty customerIdProperty() { return customerId; }


    // Regular getters
    public int getSaleId() { return saleId.get(); }
    public LocalDate getSaleDate() { return saleDate.get(); }
    public LocalTime getSaleTime() { return saleTime.get(); }
    public double getSubtotal() { return subtotal.get(); }
    public double getDiscount() { return discount.get(); }
    public double getDebt() { return debt.get(); }
    public double getTotal() { return total.get(); }
    public String getCustomerName() { return customerName.get(); }
    public String getCustomerId() { return customerId.get(); }
}