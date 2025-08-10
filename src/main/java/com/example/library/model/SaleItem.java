package com.example.library.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.text.DecimalFormat;

public class SaleItem {
    private final SimpleStringProperty number;
    private final IntegerProperty saleId = new SimpleIntegerProperty();
    private final SimpleStringProperty productId;
    private final SimpleStringProperty productName;
    private final SimpleStringProperty barcode;
    private final SimpleStringProperty quantity;
    private final SimpleStringProperty price;
    private final SimpleStringProperty totalPrice;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.00");

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

    // Setters with improved validation
    public void setNumber(String number) {
        this.number.set(number != null ? number : "");
    }

    public void setProductId(String productId) {
        this.productId.set(productId != null ? productId : "");
    }

    public void setProductName(String productName) {
        this.productName.set(productName != null ? productName : "");
    }

    public void setBarcode(String barcode) {
        this.barcode.set(barcode != null ? barcode : "");
    }

    public void setQuantity(String quantity) {
        this.quantity.set(quantity != null ? quantity : "0");
        calculateTotal();
    }

    public void setPrice(String price) {
        this.price.set(price != null ? price : "0.00");
        calculateTotal();
    }

    public void setTotal(String total) {
        this.totalPrice.set(total != null ? decimalFormat.format(parseDouble(total)) : "0.00");
    }

    // Getters with null checks
    public String getNumber() {
        return number.get() != null ? number.get() : "";
    }

    public String getProductId() {
        return productId.get() != null ? productId.get() : "";
    }

    public String getProductName() {
        return productName.get() != null ? productName.get() : "";
    }

    public String getBarcode() {
        return barcode.get() != null ? barcode.get() : "";
    }

    public String getQuantity() {
        return quantity.get() != null ? quantity.get() : "0";
    }

    public String getPrice() {
        return price.get() != null ? price.get() : "0.00";
    }

    public String getTotalPrice() {
        return totalPrice.get() != null ? totalPrice.get() : "0.00";
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

    // Improved calculateTotal method
    public void calculateTotal() {
        try {
            double priceValue = parseDouble(price.get());
            int quantityValue = parseInt(quantity.get());
            double total = priceValue * quantityValue;
            this.totalPrice.set(decimalFormat.format(total));
        } catch (NumberFormatException e) {
            this.totalPrice.set("0.00");
        }
    }

    // Helper methods for safe parsing
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(" DZ", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Improved updateItem method
    public void updateItem(String productName, String barcode, String quantity, String price) {
        setProductName(productName);
        setBarcode(barcode);
        setQuantity(quantity);
        setPrice(price);
    }

    public int getSaleId() { return saleId.get(); }
}