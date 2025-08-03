package com.example.library.controller.inventory;

import com.example.library.model.Product;
import com.example.library.util.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class updateFormController {

    @FXML private TextField barcodeField;
    @FXML private TextField productNameField;
    @FXML private TextField descriptionField;
    @FXML private TextField price1Field;
    @FXML private TextField price2Field;
    @FXML private TextField price3Field;
    @FXML private MenuButton unitMenuButton;
    @FXML private TextField quantityField;
    @FXML private DatePicker productionDatePicker;
    @FXML private DatePicker expirationDatePicker;
    @FXML private ImageView productImageView;
    @FXML private Button uploadImageButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private Product currentProduct;
    private String imagePath;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        setupDatePickerFormatter(productionDatePicker);
        setupDatePickerFormatter(expirationDatePicker);
        setupPriceFieldFormatting(price1Field);
        setupPriceFieldFormatting(price2Field);
        setupPriceFieldFormatting(price3Field);

        if (price1Field.getText().isEmpty()) price1Field.setText("0.00");
        if (price2Field.getText().isEmpty()) price2Field.setText("0.00");
        if (price3Field.getText().isEmpty()) price3Field.setText("0.00");
    }

    private void setupDatePickerFormatter(DatePicker datePicker) {
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, dateFormatter) : null;
            }
        });
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
                } else if (!cleanValue.isEmpty()) {
                    cleanValue = cleanValue + ".00";
                }
                if (!cleanValue.equals(newValue)) {
                    priceField.setText(cleanValue);
                    priceField.positionCaret(cleanValue.length());
                }
            } else {
                priceField.setText("0.00");
            }
        });
    }

    public void setProductData(Product product) {
        this.currentProduct = product;
        this.imagePath = product.getImagePath();

        barcodeField.setText(product.getBarcode());
        productNameField.setText(product.getProductName());
        descriptionField.setText(product.getDescription());
        price1Field.setText(String.format("%.2f", product.getPrice1()));
        price2Field.setText(String.format("%.2f", product.getPrice2()));
        price3Field.setText(String.format("%.2f", product.getPrice3()));
        quantityField.setText(String.valueOf(product.getQuantity()));

        if (product.getUnit() != null && !product.getUnit().isEmpty()) {
            unitMenuButton.setText(product.getUnit());
        }

        productionDatePicker.setValue(product.getProductionDate());
        expirationDatePicker.setValue(product.getExpiryDate());

        // Enhanced image loading
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            loadProductImage(product.getImagePath());
        } else {
            productImageView.setImage(null);
        }
    }

    private void loadProductImage(String path) {
        try {
            // Try loading as resource first
            InputStream resourceStream = getClass().getResourceAsStream(path);
            if (resourceStream != null) {
                productImageView.setImage(new Image(resourceStream));
                return;
            }

            // Try loading with images directory prefix
            if (!path.startsWith("/images/")) {
                resourceStream = getClass().getResourceAsStream("/images/" + path);
                if (resourceStream != null) {
                    productImageView.setImage(new Image(resourceStream));
                    return;
                }
            }

            // Try loading as file path
            File imageFile = new File(path);
            if (imageFile.exists()) {
                productImageView.setImage(new Image(imageFile.toURI().toString()));
                return;
            }

            // If all attempts fail, try extracting filename
            String filename = path.contains("/") ?
                    path.substring(path.lastIndexOf("/") + 1) : path;
            resourceStream = getClass().getResourceAsStream("/images/" + filename);
            if (resourceStream != null) {
                productImageView.setImage(new Image(resourceStream));
            } else {
                productImageView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            productImageView.setImage(null);
        }
    }

    @FXML
    public void handleReadBarcode(ActionEvent actionEvent) {
        barcodeField.requestFocus();
    }

    @FXML
    public void handleUploadImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        if (selectedFile != null) {
            this.imagePath = selectedFile.getAbsolutePath();
            productImageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    public void handleDeleteImage(ActionEvent actionEvent) {
        this.imagePath = null;
        productImageView.setImage(null);
    }

    @FXML
    public void handleCancel(ActionEvent actionEvent) {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    @FXML
    public void handleClear(ActionEvent actionEvent) {
        productNameField.clear();
        descriptionField.clear();
        price1Field.setText("0.00");
        price2Field.setText("0.00");
        price3Field.setText("0.00");
        quantityField.clear();
        unitMenuButton.setText("Select Unit");
        productionDatePicker.setValue(null);
        expirationDatePicker.setValue(null);
        productImageView.setImage(null);
        imagePath = null;
    }

    @FXML
    public void handleSave(ActionEvent actionEvent) {
        if (!validateFields()) return;

        try {
            currentProduct.setProductName(productNameField.getText());
            currentProduct.setDescription(descriptionField.getText());
            currentProduct.setPrice1(Double.parseDouble(price1Field.getText()));
            currentProduct.setPrice2(Double.parseDouble(price2Field.getText()));
            currentProduct.setPrice3(Double.parseDouble(price3Field.getText()));
            currentProduct.setQuantity(Integer.parseInt(quantityField.getText()));
            currentProduct.setUnit(unitMenuButton.getText());
            currentProduct.setProductionDate(productionDatePicker.getValue());
            currentProduct.setExpiryDate(expirationDatePicker.getValue());
            currentProduct.setImagePath(imagePath);

            if (updateProductInDatabase(currentProduct)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully!");
                ((Stage) saveButton.getScene().getWindow()).close();
            } else {
                throw new Exception("Failed to update product in database");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (productNameField.getText().isEmpty() ||
                price1Field.getText().isEmpty() ||
                quantityField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Please fill all required fields (Product Name, Price 1, Quantity)");
            return false;
        }

        if (productionDatePicker.getValue() != null &&
                expirationDatePicker.getValue() != null &&
                productionDatePicker.getValue().isAfter(expirationDatePicker.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Date Error",
                    "Production date cannot be after expiration date");
            return false;
        }

        try {
            Double.parseDouble(price1Field.getText());
            if (!price2Field.getText().isEmpty()) Double.parseDouble(price2Field.getText());
            if (!price3Field.getText().isEmpty()) Double.parseDouble(price3Field.getText());
            Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Please enter valid numbers for prices and quantity");
            return false;
        }

        return true;
    }

    private boolean updateProductInDatabase(Product product) {
        String query = "UPDATE products SET " +
                "product_name = ?, description = ?, " +
                "price1 = ?, price2 = ?, price3 = ?, " +
                "quantity = ?, unit = ?, " +
                "production_date = ?, expiration_date = ?, " +
                "image_path = ? WHERE barcode = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice1());
            stmt.setDouble(4, product.getPrice2());
            stmt.setDouble(5, product.getPrice3());
            stmt.setInt(6, product.getQuantity());
            stmt.setString(7, product.getUnit());
            stmt.setObject(8, product.getProductionDate());
            stmt.setObject(9, product.getExpiryDate());
            stmt.setString(10, product.getImagePath());
            stmt.setString(11, product.getBarcode());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}