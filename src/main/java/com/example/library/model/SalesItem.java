package com.example.library.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

// Data model for TableView
public class SalesItem {
    private final SimpleIntegerProperty number;
    private final SimpleStringProperty barcode;
    private final SimpleStringProperty productName;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty price;
    private final SimpleDoubleProperty totalPrice;

    public SalesItem(int number, String barcode, String productName, int quantity, double price) {
        this.number = new SimpleIntegerProperty(number);
        this.barcode = new SimpleStringProperty(barcode);
        this.productName = new SimpleStringProperty(productName);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.price = new SimpleDoubleProperty(price);
        this.totalPrice = new SimpleDoubleProperty(price * quantity);
    }

    public SimpleIntegerProperty numberProperty() {
        return number;
    }

    public SimpleStringProperty barcodeProperty() {
        return barcode;
    }

    public SimpleStringProperty productNameProperty() {
        return productName;
    }

    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    public SimpleDoubleProperty priceProperty() {
        return price;
    }

    public SimpleDoubleProperty totalPriceProperty() {
        return totalPrice;
    }

    public double getTotalPrice() {
        return totalPrice.get();
    }
}