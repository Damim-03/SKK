package com.example.library.controller.ProductGateWay;

import com.example.library.model.ProductShow;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class ProductPrice {

    @FXML private ImageView productImage;
    @FXML private TextField productNameField;
    @FXML private TextField priceField;

    @FXML
    public void initialize() {
        // Style text fields for readability
        productNameField.setStyle("-fx-font-size: 22px; -fx-alignment: center;");
        priceField.setStyle("-fx-font-size: 35px; -fx-alignment: center;");
    }

    /**
     * Display product details in the UI
     */
    public void showProduct(ProductShow product) {
        if (product == null) return;


        productNameField.setText(product.getProductName() != null ? product.getProductName() : "غير متوفر");

        // Price with "DZ" suffix
        String priceText = (product.getPrice1() != null ? product.getPrice1().toString() + " DZ" : "غير متوفر");
        priceField.setText(priceText);

        // Default image path (inside resources)
        String defaultImagePath = "/images/default-product.png";

        try {
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                File file = new File(product.getImagePath());
                if (file.exists()) {
                    productImage.setImage(new Image(file.toURI().toString()));
                    return;
                }
            }
            // Load fallback image if not found
            productImage.setImage(new Image(getClass().getResourceAsStream(defaultImagePath)));
        } catch (Exception e) {
            System.out.println("Error loading product image: " + e.getMessage());
            productImage.setImage(null);
        }
    }
}
