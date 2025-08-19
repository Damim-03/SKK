package com.example.library.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class Purchase {
    private final IntegerProperty purchaseId = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> purchaseDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> purchaseTime = new SimpleObjectProperty<>();
    private final DoubleProperty subtotal = new SimpleDoubleProperty();
    private final DoubleProperty discount = new SimpleDoubleProperty();
    private final DoubleProperty debt = new SimpleDoubleProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();
    private final StringProperty customerName = new SimpleStringProperty();
    private final StringProperty customerId = new SimpleStringProperty();

    public Purchase(int purchaseId, LocalDate purchaseDate, LocalTime purchaseTime, double subtotal,
                double discount, double debt, double total, String customerName, String customerId) {
        this.purchaseId.set(purchaseId);
        this.purchaseDate.set(purchaseDate);
        this.purchaseTime.set(purchaseTime);
        this.subtotal.set(subtotal);
        this.discount.set(discount);
        this.debt.set(debt);
        this.total.set(total);
        this.customerName.set(customerName);
        this.customerId.set(customerId);
    }

    // Property getters
    public IntegerProperty saleIdProperty() { return purchaseId; }
    public ObjectProperty<LocalDate> saleDateProperty() { return purchaseDate; }
    public ObjectProperty<LocalTime> saleTimeProperty() { return purchaseTime; }
    public DoubleProperty subtotalProperty() { return subtotal; }
    public DoubleProperty discountProperty() { return discount; }
    public DoubleProperty debtProperty() { return debt; }
    public DoubleProperty totalProperty() { return total; }
    public StringProperty customerNameProperty() { return customerName; }
    public StringProperty customerIdProperty() { return customerId; }


    // Regular getters
    public int getPurchaseId() { return purchaseId.get(); }
    public LocalDate getPurchaseDate() { return purchaseDate.get(); }
    public LocalTime getPurchaseTime() { return purchaseTime.get(); }
    public double getSubtotal() { return subtotal.get(); }
    public double getDiscount() { return discount.get(); }
    public double getDebt() { return debt.get(); }
    public double getTotal() { return total.get(); }
    public String getCustomerName() { return customerName.get(); }
    public String getCustomerId() { return customerId.get(); }
}
