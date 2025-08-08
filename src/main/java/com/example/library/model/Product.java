package com.example.library.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Product {
    private final StringProperty barcode = new SimpleStringProperty();
    private final StringProperty productName = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final DoubleProperty price1 = new SimpleDoubleProperty();
    private final DoubleProperty price2 = new SimpleDoubleProperty();
    private final DoubleProperty price3 = new SimpleDoubleProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final StringProperty unit = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty(); // ✅ new field
    private final ObjectProperty<LocalDate> productionDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> expiryDate = new SimpleObjectProperty<>();
    private final StringProperty imagePath = new SimpleStringProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public Product() {
        // Default constructor
    }

    public Product(String barcode, String productName, String description,
                   double price1, double price2, double price3,
                   int quantity, String unit, String category,
                   LocalDate productionDate, LocalDate expiryDate,
                   String imagePath) {
        setBarcode(barcode);
        setProductName(productName);
        setDescription(description);
        setPrice1(price1);
        setPrice2(price2);
        setPrice3(price3);
        setQuantity(quantity);
        setUnit(unit);
        setCategory(category); // ✅
        setProductionDate(productionDate);
        setExpiryDate(expiryDate);
        setImagePath(imagePath);
    }

    // Property getters
    public StringProperty barcodeProperty() { return barcode; }
    public StringProperty productNameProperty() { return productName; }
    public StringProperty descriptionProperty() { return description; }
    public DoubleProperty price1Property() { return price1; }
    public DoubleProperty price2Property() { return price2; }
    public DoubleProperty price3Property() { return price3; }
    public IntegerProperty quantityProperty() { return quantity; }
    public StringProperty unitProperty() { return unit; }
    public StringProperty categoryProperty() { return category; } // ✅
    public ObjectProperty<LocalDate> productionDateProperty() { return productionDate; }
    public ObjectProperty<LocalDate> expiryDateProperty() { return expiryDate; }
    public StringProperty imagePathProperty() { return imagePath; }
    public BooleanProperty selectedProperty() { return selected; }

    // Regular getters
    public String getBarcode() { return barcode.get(); }
    public String getProductName() { return productName.get(); }
    public String getDescription() { return description.get(); }
    public double getPrice1() { return price1.get(); }
    public double getPrice2() { return price2.get(); }
    public double getPrice3() { return price3.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getUnit() { return unit.get(); }
    public String getCategory() { return category.get(); } // ✅
    public LocalDate getProductionDate() { return productionDate.get(); }
    public LocalDate getExpiryDate() { return expiryDate.get(); }
    public String getImagePath() { return imagePath.get(); }
    public boolean isSelected() { return selected.get(); }

    // Setters
    public void setBarcode(String barcode) { this.barcode.set(barcode); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public void setDescription(String description) { this.description.set(description); }
    public void setPrice1(double price1) { this.price1.set(price1); }
    public void setPrice2(double price2) { this.price2.set(price2); }
    public void setPrice3(double price3) { this.price3.set(price3); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public void setUnit(String unit) { this.unit.set(unit); }
    public void setCategory(String category) { this.category.set(category); } // ✅
    public void setProductionDate(LocalDate productionDate) { this.productionDate.set(productionDate); }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate.set(expiryDate); }
    public void setImagePath(String imagePath) { this.imagePath.set(imagePath); }
    public void setSelected(boolean selected) { this.selected.set(selected); }
}