package com.example.library.controller.ProductGateWay;

import com.example.library.model.ProductShow;
import com.example.library.util.DatabaseConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;

public class ProductShowPrices {
    @FXML private TextField barcodeField;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> debounceTask;
    private Stage productStage;

    @FXML
    public void initialize() {
        barcodeField.setEditable(true);
        barcodeField.setFocusTraversable(true);

        // Initialize reusable Stage
        productStage = new Stage();
        productStage.setTitle("Soubirate Kamel Kir");
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/SKK-1.png"));
            productStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("لم يتم العثور على الأيقونة: " + e.getMessage());
        }
        productStage.setOnCloseRequest(event -> resetBarcodeField());

        // Debounce for barcode scanning
        barcodeField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty() && !newValue.equals(oldValue)) {
                if (debounceTask != null) debounceTask.cancel(false);
                debounceTask = executor.schedule(() ->
                        Platform.runLater(() -> handleBarcodeScan(newValue)), 500, TimeUnit.MILLISECONDS);
            }
        });
    }

    private void handleBarcodeScan(String barcode) {
        // Run query async
        CompletableFuture.supplyAsync(() -> {
            try {
                return loadProductFromDatabase(barcode);
            } catch (SQLException e) {
                Platform.runLater(() -> showError("حدث خطأ أثناء تحميل بيانات المنتج"));
                return null;
            }
        }).thenAccept(product -> Platform.runLater(() -> showProductStage(product)));
    }

    private void showProductStage(ProductShow product) {
        try {
            Parent root;
            if (product != null) {
                // Product found → load Product UI
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/Product/ProductShowui.fxml"));
                root = loader.load();
                ProductPrice controller = loader.getController();
                controller.showProduct(product);
            } else {
                // Product not found → load Not Found UI
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/Product/ProductNotFound.fxml"));
                root = loader.load();
            }

            Scene scene = new Scene(root);
            productStage.setScene(scene);
            productStage.setMaximized(true);

            if (!productStage.isShowing()) {
                productStage.show();
            }

            // Auto-close after timeout
            int timeoutSeconds = (product != null) ? 5 : 3;
            executor.schedule(() -> Platform.runLater(() -> {
                productStage.hide();
                resetBarcodeField();
            }), timeoutSeconds, TimeUnit.SECONDS);

        } catch (Exception e) {
            showError("حدث خطأ أثناء تحميل واجهة المنتج");
        }
    }

    private void resetBarcodeField() {
        barcodeField.clear();
        barcodeField.requestFocus();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("خطأ");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private ProductShow loadProductFromDatabase(String barcode) throws SQLException {
        String query = "SELECT id, barcode, product_name, price1, image_path FROM products WHERE barcode = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, barcode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ProductShow product = new ProductShow();
                    product.setId(rs.getString("id"));
                    product.setBarcode(rs.getString("barcode"));
                    product.setProductName(rs.getString("product_name"));
                    product.setPrice1(rs.getBigDecimal("price1"));
                    product.setImagePath(rs.getString("image_path"));
                    return product;
                }
            }
        }
        return null;
    }
}
