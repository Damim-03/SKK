package com.example.library.model;

import java.time.LocalDate;

public class ProductShow {
    private String id;
    private String barcode;
    private String productName;
    private String description;
    private java.math.BigDecimal price1;
    private java.math.BigDecimal price2;
    private java.math.BigDecimal price3;
    private String unit;
    private int quantity;
    private LocalDate expirationDate;
    private String imagePath;
    private String category;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public java.math.BigDecimal getPrice1() { return price1; }
    public void setPrice1(java.math.BigDecimal price1) { this.price1 = price1; }

    public java.math.BigDecimal getPrice2() { return price2; }
    public void setPrice2(java.math.BigDecimal price2) { this.price2 = price2; }

    public java.math.BigDecimal getPrice3() { return price3; }
    public void setPrice3(java.math.BigDecimal price3) { this.price3 = price3; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
