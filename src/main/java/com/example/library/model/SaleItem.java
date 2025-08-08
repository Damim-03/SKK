package com.example.library.model;

import javafx.beans.property.SimpleStringProperty;

public class SaleItem {
    private final SimpleStringProperty number;
    private final SimpleStringProperty productId;
    private final SimpleStringProperty productName;
    private final SimpleStringProperty barcode;
    private final SimpleStringProperty quantity;
    private final SimpleStringProperty price;
    private final SimpleStringProperty totalPrice;

    public SaleItem(String number, String productId, String productName, String barcode,
                    String quantity, String price, String totalPrice) {
        this.number = new SimpleStringProperty(number);
        this.productId = new SimpleStringProperty(productId);
        this.productName = new SimpleStringProperty(productName);
        this.barcode = new SimpleStringProperty(barcode);
        this.quantity = new SimpleStringProperty(quantity);
        this.price = new SimpleStringProperty(price);
        this.totalPrice = new SimpleStringProperty(totalPrice);
    }

    // Setters
    public void setNumber(String number) {
        this.number.set(number);
    }

    public void setProductId(String productId) {
        this.productId.set(productId);
    }

    public void setProductName(String productName) {
        this.productName.set(productName);
    }

    public void setBarcode(String barcode) {
        this.barcode.set(barcode);
    }

    public void setQuantity(String quantity) {
        this.quantity.set(quantity);
    }

    public void setPrice(String price) {
        this.price.set(price);
        calculateTotal();
    }

    public void setTotal(String total) {
        this.totalPrice.set(total);
    }

    // Getters
    public String getNumber() {
        return number.get();
    }

    public String getProductId() {
        return productId.get();
    }

    public String getProductName() {
        return productName.get();
    }

    public String getBarcode() {
        return barcode.get();
    }

    public String getQuantity() {
        return quantity.get();
    }

    public String getPrice() {
        return price.get();
    }

    public String getTotalPrice() {
        return totalPrice.get();
    }

    // Property getters for TableView
    public SimpleStringProperty numberProperty() {
        return number;
    }

    public SimpleStringProperty productIdProperty() {
        return productId;
    }

    public SimpleStringProperty productNameProperty() {
        return productName;
    }

    public SimpleStringProperty barcodeProperty() {
        return barcode;
    }

    public SimpleStringProperty quantityProperty() {
        return quantity;
    }

    public SimpleStringProperty priceProperty() {
        return price;
    }

    public SimpleStringProperty totalPriceProperty() {
        return totalPrice;
    }

    // Helper method to calculate total price
    public void calculateTotal() {
        try {
            double priceValue = Double.parseDouble(price.get().replace(" DZ", "").trim());
            int quantityValue = Integer.parseInt(quantity.get());
            double total = priceValue * quantityValue;
            this.totalPrice.set(String.format("%.2f", total));
        } catch (NumberFormatException e) {
            this.totalPrice.set("0.00");
        }
    }

    // Method to update all fields at once
    public void updateItem(String productName, String barcode, String quantity, String price) {
        setProductName(productName);
        setBarcode(barcode);
        setQuantity(quantity);
        setPrice(price); // This will automatically calculate total
    }
}