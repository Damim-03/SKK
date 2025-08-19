package com.example.library.controller.client;

import com.example.library.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.function.UnaryOperator;

import static com.example.library.Alert.alert.*;

public class addproductSalesClientController {

    @FXML private TextField generatedBarcode;
    @FXML private Button generatedBarcodeButton;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private Button addButton;
    @FXML private Button cancelButton;

    private salesClientController mainController;
    private String customerId;
    private boolean isFromDatabase;

    public void setMainController(salesClientController controller) {
        this.mainController = controller;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @FXML
    private void initialize() {
        // Setup barcode generation
        generatedBarcodeButton.setOnAction(e -> {
            String uniqueBarcode = generateUniqueBarcode();
            generatedBarcode.setText(uniqueBarcode);
        });

        // Setup buttons
        addButton.setOnAction(e -> handleAddProduct());
        cancelButton.setOnAction(e -> ((Stage) cancelButton.getScene().getWindow()).close());

        // Price field validation (numbers with 2 decimal places)
        priceField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            }
            return null;
        }));

        // Quantity field validation (only positive integers)
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        };
        quantityField.setTextFormatter(new TextFormatter<>(integerFilter));

        // Format price when focus is lost
        priceField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                formatPriceField();
            }
        });
    }

    private void formatPriceField() {
        String priceText = priceField.getText();
        try {
            double price = Double.parseDouble(priceText);
            priceField.setText(String.format("%.2f", price));
        } catch (NumberFormatException e) {
            priceField.setText("0.00");
        }
    }

    private void handleAddProduct() {
        // Get values from fields
        String name = nameField.getText().trim();
        String barcode = generatedBarcode.getText().trim();
        String priceText = priceField.getText().trim();
        String quantityText = quantityField.getText().trim();

        // Validate required fields
        if (name.isEmpty() || priceText.isEmpty() || quantityText.isEmpty() || barcode.isEmpty()) {
            showWarningAlert("خطأ في الإدخال", "جميع الحقول (بما في ذلك الرمز الشريطي) مطلوبة.");
            return;
        }

        // Parse and validate price
        double priceVal;
        try {
            priceVal = Double.parseDouble(priceText);
            if (priceVal <= 0.0) {
                showWarningAlert("سعر غير صالح", "يجب أن يكون السعر أكبر من 0.");
                return;
            }
            priceVal = Double.parseDouble(String.format("%.2f", priceVal));
        } catch (NumberFormatException e) {
            showFailedAlert("خطأ في التنسيق", "يجب أن يكون السعر رقمًا صالحًا.");
            return;
        }

        // Parse and validate quantity
        int quantityVal;
        try {
            quantityVal = Integer.parseInt(quantityText);
            if (quantityVal <= 0) {
                showWarningAlert("الكمية غير صالحة", "يجب أن تكون الكمية أكبر من 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showFailedAlert("خطأ في التنسيق", "يجب أن تكون الكمية عددًا صحيحًا.");
            return;
        }

        // Calculate total
        double total = priceVal * quantityVal;
        total = Double.parseDouble(String.format("%.2f", total));

        // Add product to main table
        if (mainController != null) {
            mainController.addProductToTable(name, barcode, priceVal, quantityVal, total, isFromDatabase);
        }

        // Close the window
        ((Stage) addButton.getScene().getWindow()).close();
    }

    private String generateUniqueBarcode() {
        String barcode;
        int attempts = 0;
        do {
            barcode = generateValid13DigitBarcode();
            attempts++;
            if (attempts > 100) {
                showFailedAlert("خطأ", "تعذر إنشاء رمز منتج فريد بعد 100 محاولة.");
                return "";
            }
        } while (barcodeExistsInDatabase(barcode));
        return barcode;
    }

    private String generateValid13DigitBarcode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(random.nextInt(10));
        }
        int checksum = calculateEAN13Checksum(sb.toString());
        sb.append(checksum);
        return sb.toString();
    }

    private int calculateEAN13Checksum(String barcode) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int mod = sum % 10;
        return (10 - mod) % 10;
    }

    private boolean barcodeExistsInDatabase(String barcode) {
        String query = "SELECT COUNT(*) FROM products WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            showFailedAlert("خطأ", "فشل التحقق من رمز المنتج: " + e.getMessage());
            return true;
        }
        return false;
    }
}