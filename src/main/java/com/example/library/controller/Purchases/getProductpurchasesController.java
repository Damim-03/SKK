package com.example.library.controller.Purchases;

import com.example.library.model.Product;
import com.example.library.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.example.library.Alert.alert.showFailedAlert;

public class getProductpurchasesController {

    @FXML
    private TableView<Product> tableView;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, Boolean> selectColumn;

    @FXML private TextField barcodeField;
    @FXML private TextField nameField;
    @FXML private TextField productionDateField;
    @FXML private TextField expiryDateField;
    @FXML private TextField price1Field;
    @FXML private TextField price2Field;
    @FXML private TextField price3Field;
    @FXML private TextField unitField;
    @FXML private ImageView productImage;
    @FXML private Label categoryLabel; // ✅ optional label for display
    @FXML private TextField categoryField; // ✅ optional TextField for form

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<Product> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadDataFromDatabase();
        setupListeners();
    }

    private void setupTableColumns() {
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price1"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });
    }

    private void setupListeners() {
        barcodeField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterTableByBarcode(newVal);
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                updateFormFields(newSel);
            }
        });
    }

    private void loadDataFromDatabase() {
        String query = "SELECT barcode, product_name, description, price1, price2, price3, " +
                "quantity, unit, category, production_date, expiration_date, image_path FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            productList.clear();

            while (rs.next()) {
                LocalDate productionDate = rs.getDate("production_date") != null ?
                        rs.getDate("production_date").toLocalDate() : null;
                LocalDate expirationDate = rs.getDate("expiration_date") != null ?
                        rs.getDate("expiration_date").toLocalDate() : null;

                Product product = new Product(
                        rs.getString("barcode"),
                        rs.getString("product_name"),
                        rs.getString("description"),
                        rs.getDouble("price1"),
                        rs.getDouble("price2"),
                        rs.getDouble("price3"),
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getString("category"),
                        productionDate,
                        expirationDate,
                        rs.getString("image_path")
                );

                productList.add(product);
            }

            filteredList.setAll(productList);
            tableView.setItems(filteredList);
            tableView.getSelectionModel().clearSelection();
            tableView.refresh();

        } catch (SQLException e) {
            showFailedAlert("فشل", "فشل تحميل المنتج: " + e.getMessage());
        }
    }

    private void updateFormFields(Product product) {
        if (product == null) {
            clearFormFields();
            return;
        }

        barcodeField.setText(product.getBarcode());
        nameField.setText(product.getProductName());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        productionDateField.setText(product.getProductionDate() != null ?
                formatter.format(product.getProductionDate()) : "");
        expiryDateField.setText(product.getExpiryDate() != null ?
                formatter.format(product.getExpiryDate()) : "");

        price1Field.setText(String.format("%.2f", product.getPrice1()));
        price2Field.setText(String.format("%.2f", product.getPrice2()));
        price3Field.setText(String.format("%.2f", product.getPrice3()));

        unitField.setText(product.getUnit());

        // ✅ Display category
        if (categoryField != null) {
            categoryField.setText(product.getCategory());
        } else if (categoryLabel != null) {
            categoryLabel.setText(product.getCategory());
        }

        Image image = loadProductImage(product.getImagePath());
        productImage.setImage(image);
        productImage.setVisible(image != null);
    }

    private Image loadProductImage(String path) {
        if (path == null || path.isBlank()) {
            return loadFallbackImage();
        }

        try {
            String normalizedPath = path.startsWith("/") ? path : "/" + path;

            InputStream resourceStream = getClass().getResourceAsStream(normalizedPath);
            if (resourceStream != null) {
                return new Image(resourceStream);
            }

            if (!normalizedPath.startsWith("/images/")) {
                resourceStream = getClass().getResourceAsStream("/images/" + path);
                if (resourceStream != null) {
                    return new Image(resourceStream);
                }
            }

            File file = new File(path);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }

            URL url = getClass().getResource(normalizedPath);
            if (url != null) {
                File classpathFile = new File(url.toURI());
                if (classpathFile.exists()) {
                    return new Image(classpathFile.toURI().toString());
                }
            }

            String filename = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;
            resourceStream = getClass().getResourceAsStream("/images/" + filename);
            if (resourceStream != null) {
                return new Image(resourceStream);
            }

        } catch (Exception e) {
            showFailedAlert("فشل: ", "خطأ في تحميل الصورة" + e.getMessage());
        }

        return loadFallbackImage();
    }

    private Image loadFallbackImage() {
        try {
            InputStream stream = getClass().getResourceAsStream("/images/image.png");
            if (stream == null) {
                stream = getClass().getResourceAsStream("image.png");
            }
            if (stream != null) {
                return new Image(stream);
            }
        } catch (Exception e) {
            showFailedAlert("فشل", "خطأ في ارجاع الصورة" + e.getMessage());
        }
        return null;
    }

    private void clearFormFields() {
        barcodeField.clear();
        nameField.clear();
        productionDateField.clear();
        expiryDateField.clear();
        price1Field.clear();
        price2Field.clear();
        price3Field.clear();
        unitField.clear();
        if (categoryField != null) categoryField.clear(); // ✅
        productImage.setImage(null);
    }

    private void filterTableByBarcode(String barcode) {
        filteredList.clear();
        if (barcode == null || barcode.isBlank()) {
            filteredList.addAll(productList);
        } else {
            productList.stream()
                    .filter(p -> p.getBarcode().equalsIgnoreCase(barcode))
                    .findFirst()
                    .ifPresent(product -> {
                        filteredList.add(product);
                        if (!tableView.getItems().isEmpty()) {
                            tableView.getSelectionModel().select(product);
                        }
                    });
        }
        tableView.refresh();
    }

    @FXML
    private void handleReadBarcode() {
        barcodeField.requestFocus();
        barcodeField.clear();
    }

    @FXML
    private void handleCleanButton() {
        clearFormFields();
    }

    @FXML
    private void handleUpdateButton() {
        loadDataFromDatabase();
    }
}
