package com.example.library.controller.inventory;

import com.example.library.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.InputStream;
import java.sql.*;

public class productShowController {

    @FXML private TextField barcodeField;
    @FXML private TextField productNameField;
    @FXML private TextField descriptionField;
    @FXML private TextField price1Field;
    @FXML private TextField price2Field;
    @FXML private TextField price3Field;
    @FXML private TextField quantityField;
    @FXML private TextField unitField;
    @FXML private DatePicker productionDatePicker;
    @FXML private DatePicker expirationDatePicker;
    @FXML private ImageView productImageView;
    @FXML private Button cancelButton;

    @FXML
    public void initialize() {
        setupFields();
    }

    private void setupFields() {
        barcodeField.setEditable(false);
        productNameField.setEditable(false);
        descriptionField.setEditable(false);
        price1Field.setEditable(false);
        price2Field.setEditable(false);
        price3Field.setEditable(false);
        quantityField.setEditable(false);
        unitField.setEditable(false);
        productionDatePicker.setEditable(false);
        expirationDatePicker.setEditable(false);
    }

    public void loadProductData(String barcode) {
        String query = "SELECT * FROM products WHERE barcode = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                barcodeField.setText(rs.getString("barcode"));
                productNameField.setText(rs.getString("product_name"));
                descriptionField.setText(rs.getString("description"));
                price1Field.setText(String.valueOf(rs.getDouble("price1")));
                price2Field.setText(String.valueOf(rs.getDouble("price2")));
                price3Field.setText(String.valueOf(rs.getDouble("price3")));
                quantityField.setText(String.valueOf(rs.getInt("quantity")));
                unitField.setText(String.valueOf(rs.getString("unit")));

                Date productionDate = rs.getDate("production_date");
                if (productionDate != null)
                    productionDatePicker.setValue(productionDate.toLocalDate());

                Date expirationDate = rs.getDate("expiration_date");
                if (expirationDate != null)
                    expirationDatePicker.setValue(expirationDate.toLocalDate());

                // Load image from resource, relative path, or file path
                String imagePath = rs.getString("image_path");
                loadProductImage(imagePath);
            } else {
                showAlert("المنتج غير موجود", "لم يتم العثور على منتج بالباركود: " + barcode);
            }
        } catch (SQLException e) {
            showAlert("خطأ في قاعدة البيانات", "فشل تحميل المنتج: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProductImage(String path) {
        try {
            if (path == null || path.isEmpty()) {
                System.out.println("Image path is empty, loading default image.");
                productImageView.setImage(new Image(getClass().getResourceAsStream("/images/image.png")));
                return;
            }

            File file = new File(path);
            if (file.exists()) {
                System.out.println("Loading image from file: " + path);
                productImageView.setImage(new Image(file.toURI().toString()));
                return;
            }

            InputStream is = getClass().getResourceAsStream("/images/" + path);
            if (is != null) {
                System.out.println("Loading image from resources: /images/" + path);
                productImageView.setImage(new Image(is));
                return;
            }

            System.out.println("Image not found, loading default image.");
            productImageView.setImage(new Image(getClass().getResourceAsStream("/images/image.png")));
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            productImageView.setImage(new Image(getClass().getResourceAsStream("/images/image.png")));
        }
    }


    @FXML
    private void handleCancel() {
        cancelButton.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setProductData(String barcode) {
        loadProductData(barcode);
    }
}
