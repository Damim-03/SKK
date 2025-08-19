package com.example.library.controller.client;

import com.example.library.model.SaleItem;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.HashSet;
import java.util.Set;

import static com.example.library.Alert.alert.showFailedAlert;

public class updateProductTableClientController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField generatedBarcode;
    @FXML
    private Button cancelButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button generatedBarcodeButton;

    private static final Set<String> usedBarcodes = new HashSet<>();
    private salesClientController mainController;
    private SaleItem originalItem;

    public void setMainController(salesClientController controller) {
        this.mainController = controller;
    }

    @FXML
    public void initialize() {
        setupDecimalField(priceField);
        setupIntegerField(quantityField);

        // Auto-format price while typing
        priceField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String priceText = priceField.getText();
                try {
                    double price = Double.parseDouble(priceText);
                    priceField.setText(String.format("%.2f", price));
                } catch (NumberFormatException e) {
                    priceField.setText("0.00");
                }
            }
        });

        // Final format when focus is lost
        priceField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                formatPriceField();
            }
        });

        // Cancel button
        cancelButton.setOnAction(e -> ((Stage) cancelButton.getScene().getWindow()).close());

        // Update button logic
        updateButton.setOnAction(e -> {
            if (validateInput()) {
                updateItem();
                ((Stage) updateButton.getScene().getWindow()).close();
            }
        });

        // Generate unique barcode
        generatedBarcodeButton.setOnAction(e -> {
            String barcode;
            do {
                barcode = "BAR" + (int)(Math.random() * 1000000);
            } while (usedBarcodes.contains(barcode));
            usedBarcodes.add(barcode);
            generatedBarcode.setText(barcode);
        });
    }

    private void formatPriceField() {
        String priceText = priceField.getText().replace(" DZ", "").trim();
        try {
            double price = Double.parseDouble(priceText);
            priceField.setText(String.format("%.2f DZ", price));
        } catch (NumberFormatException e) {
            priceField.setText("0.00 DZ");
        }
    }

    private boolean validateInput() {
        if (nameField.getText().isEmpty()) {
            showFailedAlert("خطأ", "يرجى إدخال اسم المنتج");
            return false;
        }

        try {
            Double.parseDouble(priceField.getText().replace(" DZ", "").trim());
            Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            showFailedAlert("خطأ", "يرجى إدخال قيم صحيحة للسعر والكمية");
            return false;
        }

        return true;
    }

    private void updateItem() {
        String name = nameField.getText();
        String price = priceField.getText().replace(" DZ", "").trim();
        String quantity = quantityField.getText();
        String barcode = generatedBarcode.getText();

        originalItem.setProductName(name);
        originalItem.setPrice(price);
        originalItem.setQuantity(quantity);
        originalItem.setBarcode(barcode);

        double total = Double.parseDouble(price) * Integer.parseInt(quantity);
        originalItem.setTotal(String.format("%.2f", total));

        if (mainController != null) {
            mainController.refreshTable();
            mainController.updateTotals();
        }
    }

    public void setProductData(SaleItem item) {
        this.originalItem = item;
        nameField.setText(item.getProductName());

        String priceText = item.getPrice();
        if (!priceText.contains("DZ")) {
            priceText += " DZ";
        }
        priceField.setText(priceText);

        quantityField.setText(item.getQuantity());
        generatedBarcode.setText(item.getBarcode());
    }

    private void setupDecimalField(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,9}(\\.\\d{0,2})?")) {
                field.setText(oldValue);
            }
        });
    }

    private void setupIntegerField(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                field.setText(oldVal);
            }
        });
    }
}
