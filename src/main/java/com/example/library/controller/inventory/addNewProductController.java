package com.example.library.controller.inventory;

import java.io.File;
import java.sql.*;
import java.util.Objects;
import java.util.UUID;
import com.example.library.model.CategoryLoader;
import com.example.library.model.UnitLoader;
import com.example.library.util.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import static com.example.library.Alert.alert.*;

public class addNewProductController {

    @FXML
    public Button saveButton;
    @FXML
    private TextField barcodeTextField;
    @FXML
    private Button readButton;
    @FXML
    private TextField productNameTextField;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private MenuButton unitMenuButton;
    @FXML
    private TextField price1TextField;
    @FXML
    private TextField price2TextField;
    @FXML
    private TextField price3TextField;
    @FXML
    private TextField quantityTextField;
    @FXML
    private ImageView productImageView;
    @FXML
    private Button uploadImageButton;
    @FXML
    private Button deleteImageButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button clearButton;
    @FXML
    private DatePicker productionDatePicker; // Added for production date
    @FXML
    private DatePicker expirationDatePicker; // Added for expiration date
    @FXML
    private MenuButton categoryMenuButton;

    private File selectedImageFile;

    public void initialize() {
        UnitLoader.loadUnitsIntoMenuButton(unitMenuButton);
        unitMenuButton.setText("Select Unit");

        CategoryLoader.loadCategoriesIntoMenuButton(categoryMenuButton);
        categoryMenuButton.setText("Select Category");

        // Allow only numeric input in the barcode field
        barcodeTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                barcodeTextField.setText(newText.replaceAll("[^\\d]", ""));
            }
        });

        // When scanner presses Enter, move focus to next field
        barcodeTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String scannedBarcode = barcodeTextField.getText().trim();
                if (!scannedBarcode.isEmpty()) {
                    productNameTextField.requestFocus(); // Move to next input
                }
            }
        });

        // Format price and quantity fields
        setupPriceFieldFormatting(price1TextField);
        setupPriceFieldFormatting(price2TextField);
        setupPriceFieldFormatting(price3TextField);
        setupQuantityFieldFormatting(quantityTextField);
    }

    private void setupPriceFieldFormatting(TextField priceField) {
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                String cleanValue = newValue.replaceAll("[^0-9.]", "");

                String[] parts = cleanValue.split("\\.");
                if (parts.length > 2) {
                    cleanValue = parts[0] + "." + parts[1];
                }

                if (cleanValue.startsWith(".")) {
                    cleanValue = "0" + cleanValue;
                }

                if (cleanValue.contains(".")) {
                    String[] decimalParts = cleanValue.split("\\.");
                    String integerPart = decimalParts[0];
                    String decimalPart = decimalParts.length > 1 ? decimalParts[1] : "";
                    decimalPart = decimalPart.length() > 2 ? decimalPart.substring(0, 2) : decimalPart;
                    cleanValue = integerPart + "." + String.format("%-2s", decimalPart).replace(' ', '0');
                } else {
                    cleanValue = cleanValue + ".00";
                }

                priceField.setText(cleanValue);
                priceField.positionCaret(cleanValue.length());
            } else {
                priceField.setText("0.00");
            }
        });
    }

    private void setupQuantityFieldFormatting(TextField quantityField) {
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                quantityField.setText(newValue.replaceAll("[^0-9]", ""));
            }
        });
    }

    @FXML
    public void handleUploadImageAction(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
            );
            Stage stage = (Stage) uploadImageButton.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                this.selectedImageFile = selectedFile; // ✅ IMPORTANT
                Image image = new Image(selectedFile.toURI().toString());
                productImageView.setImage(image);
            }
        } catch (Exception e) {
            showFailedAlert("خطأ", "فشل تحميل الصورة");
        }
    }

    @FXML
    public void handleDeleteImageAction(ActionEvent event) {
        try {
            productImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/image.png"))));
        } catch (Exception e) {
            showFailedAlert("خطأ", "فشل حذف الصورة");
        }
    }

    @FXML
    public void handleReadButtonAction(ActionEvent event) {
        barcodeTextField.requestFocus();
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> barcodeTextField.requestFocus());
            }
        }, 100);
    }

    @FXML
    public void handleBackButtonAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void handleSaveProductAction() {
        String barcode = barcodeTextField.getText();
        String name = productNameTextField.getText();
        String description = descriptionTextField.getText();
        String price1 = price1TextField.getText();
        String price2 = price2TextField.getText();
        String price3 = price3TextField.getText();
        String quantity = quantityTextField.getText();
        String unit = unitMenuButton.getText();
        String category = categoryMenuButton.getText(); // <-- Get selected category
        String productionDate = (productionDatePicker.getValue() != null) ? productionDatePicker.getValue().toString() : null;
        String expirationDate = (expirationDatePicker.getValue() != null) ? expirationDatePicker.getValue().toString() : null;
        String imagePath = (selectedImageFile != null) ? selectedImageFile.getAbsolutePath() : null;

        if (barcode.isEmpty() || name.isEmpty() || unit.equals("Select Unit") || category.equals("Select Category") || quantity.isEmpty()) {
            showWarningAlert("تنبيه", "يجب ادخال جميع المعلومات المطلوبة!");
            return;
        }

        String sql = "INSERT INTO products (id, barcode, product_name, description, price1, price2, price3, unit, quantity, production_date, expiration_date, image_path, category) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String id = UUID.randomUUID().toString();

            stmt.setString(1, id);
            stmt.setString(2, barcode);
            stmt.setString(3, name);
            stmt.setString(4, description);
            stmt.setDouble(5, Double.parseDouble(price1));
            stmt.setDouble(6, Double.parseDouble(price2));
            stmt.setDouble(7, Double.parseDouble(price3));
            stmt.setString(8, unit);
            stmt.setInt(9, Integer.parseInt(quantity));
            stmt.setDate(10, (productionDate != null) ? Date.valueOf(productionDate) : null);
            stmt.setDate(11, (expirationDate != null) ? Date.valueOf(expirationDate) : null);
            stmt.setString(12, imagePath);
            stmt.setString(13, category);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                String status = (expirationDate != null &&
                        java.time.LocalDate.parse(expirationDate).isBefore(java.time.LocalDate.now()))
                        ? "Expired" : "Valid";

                String statusSql = "INSERT INTO product_status (barcode, status) VALUES (?, ?)";
                try (PreparedStatement statusStmt = conn.prepareStatement(statusSql)) {
                    statusStmt.setString(1, barcode);
                    statusStmt.setString(2, status);
                    statusStmt.executeUpdate();
                }

                showSuccessAlert("نجاح", "تم حفظ المنتج بنجاح.");
                clearFields();
            } else {
                showFailedAlert("خطأ", "فضل في حفظ المنتج!!");
            }

        } catch (SQLException | NumberFormatException e) {
            showFailedAlert("خطأ في قاعدة البيانات", "هنالك خطأ في قاعدة البيانات الرجاء التحقق من سلامة قاعدة البيانات.");
        }
    }

    @FXML
    private void clearFields() {
        try {
        barcodeTextField.clear();
        productNameTextField.clear();
        descriptionTextField.clear();
        unitMenuButton.setText("Select Unit");
        categoryMenuButton.setText("Select Unit");
        price1TextField.clear();
        price2TextField.clear();
        price3TextField.clear();
        quantityTextField.clear();
        productionDatePicker.setValue(null);
        expirationDatePicker.setValue(null);
        productImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/image.png"))));
        } catch (Exception e) {
            showFailedAlert("خطأ", "فشل في تصفية الحقول المعلومات");
        }
    }
}