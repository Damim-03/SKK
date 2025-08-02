package com.example.library.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Product {
    private final String barcode;
    private final String productName;
    private final double price1;
    private final double price2;
    private final double price3;
    private final int quantity;
    private final String unit;
    private final String productionDate;
    private final String expiryDate;
    private final String imagePath;
    private final BooleanProperty selected;

    public Product(String barcode, String productName, double price1, double price2, double price3, int quantity, String unit, String productionDate, String expiryDate, String imagePath) {
        this.barcode = barcode;
        this.productName = productName;
        this.price1 = price1;
        this.price2 = price2;
        this.price3 = price3;
        this.quantity = quantity;
        this.unit = unit;
        this.productionDate = productionDate;
        this.expiryDate = expiryDate;
        this.imagePath = imagePath;
        this.selected = new SimpleBooleanProperty(false);
    }

    // Getters
    public String getBarcode() { return barcode; }
    public String getProductName() { return productName; }
    public double getPrice1() { return price1; }
    public double getPrice2() { return price2; }
    public double getPrice3() { return price3; }
    public int getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getProductionDate() { return productionDate; }
    public String getExpiryDate() { return expiryDate; }
    public String getImagePath() { return imagePath; }
    public BooleanProperty selectedProperty() { return selected; }
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean selected) { this.selected.set(selected); }
}