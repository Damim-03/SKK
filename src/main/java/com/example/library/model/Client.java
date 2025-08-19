package com.example.library.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Client {
    private final StringProperty customerId = new SimpleStringProperty();
    private final StringProperty customerName = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final StringProperty imagePath = new SimpleStringProperty();
    private final DoubleProperty debt = new SimpleDoubleProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty();

    public Client(String customerId, String customerName, String phone, String address, String imagePath, double debt) {
        this.customerId.set(customerId);
        this.customerName.set(customerName);
        this.phone.set(phone);
        this.address.set(address);
        this.imagePath.set(imagePath);
        this.debt.set(debt);
    }

    public String getCustomerId() {
        return customerId.get();
    }

    public StringProperty customerIdProperty() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName.get();
    }

    public StringProperty customerNameProperty() {
        return customerName;
    }

    public String getPhone() {
        return phone.get();
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public String getAddress() {
        return address.get();
    }

    public StringProperty addressProperty() {
        return address;
    }

    public String getImagePath() {
        return imagePath.get();
    }

    public StringProperty imagePathProperty() {
        return imagePath;
    }

    public double getDebt() {
        return debt.get();
    }

    public DoubleProperty debtProperty() {
        return debt;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }
}