package com.example.library.controller.inventory;

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
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.sql.*;
import java.util.Objects;

public class getProductController {

    @FXML private TableView<Product> tableView;
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

    @FXML public Button ReadBarcode;
    @FXML public Button exitButton;
    @FXML public Button updateListButton;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<Product> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadDataFromDatabase();
        filteredList.addAll(productList);
        tableView.setItems(filteredList);
        tableView.refresh();

        barcodeField.textProperty().addListener((obs, oldVal, newVal) -> filterTableByBarcode(newVal));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                updateFormFields(newSel);
            }
        });
    }

    private void setupTableColumns() {
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price1"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        barcodeColumn.setMinWidth(120);
        nameColumn.setMinWidth(130);
        priceColumn.setMinWidth(110);
        quantityColumn.setMinWidth(120);
        selectColumn.setMinWidth(40);

        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });
    }

    private void loadDataFromDatabase() {
        String query = "SELECT barcode, product_name, price1, price2, price3, quantity, unit, production_date, expiration_date, image_path FROM products";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getString("barcode"),
                        rs.getString("product_name"),
                        rs.getDouble("price1"),
                        rs.getDouble("price2"),
                        rs.getDouble("price3"),
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getString("production_date") != null ? rs.getString("production_date") : "",
                        rs.getString("expiration_date") != null ? rs.getString("expiration_date") : "",
                        rs.getString("image_path") != null ? rs.getString("image_path") : ""
                );
                productList.add(product);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateFormFields(Product product) {
        barcodeField.setText(product.getBarcode());
        nameField.setText(product.getProductName());
        productionDateField.setText(product.getProductionDate());
        expiryDateField.setText(product.getExpiryDate());
        price1Field.setText(String.valueOf(product.getPrice1()));
        price2Field.setText(String.valueOf(product.getPrice2()));
        price3Field.setText(String.valueOf(product.getPrice3()));
        unitField.setText(product.getUnit());

        Image image = loadImage(product.getImagePath());
        if (image == null) {
            image = loadFallbackImage();
        }
        productImage.setImage(image);

        if (image != null) {
            productImage.setVisible(true);
        } else {
            productImage.setVisible(false);
        }
    }

    private Image loadImage(String path) {
        if (path == null || path.isBlank()) return null;

        try {
            // 1. First try to load as absolute path (from database)
            if (path.startsWith("file:/") || path.contains(":/") || path.contains(":\\")) {
                try {
                    File file = new File(new URI(path).getPath());
                    if (file.exists()) {
                        return new Image(file.toURI().toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 2. Try to load from filesystem relative path
            File file = new File(path);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }

            // 3. Try to load from resources (classpath)
            String resourcePath = path.replace("\\", "/");
            // Remove leading slash if present
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }

            InputStream resourceStream = getClass().getResourceAsStream("/" + resourcePath);
            if (resourceStream != null) {
                return new Image(resourceStream);
            }

            // 4. Try with just the filename in default images folder
            String fileName = resourcePath.contains("/")
                    ? resourcePath.substring(resourcePath.lastIndexOf("/") + 1)
                    : resourcePath;

            resourceStream = getClass().getResourceAsStream("/images/products/" + fileName);
            if (resourceStream != null) {
                return new Image(resourceStream);
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Image loadFallbackImage() {
        try {
            // Try multiple possible locations
            InputStream stream = getClass().getResourceAsStream("/images/image.png");
            if (stream == null) {
                stream = getClass().getResourceAsStream("image.png");
            }
            if (stream == null) {
                return null;
            }
            return new Image(stream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void filterTableByBarcode(String barcode) {
        filteredList.clear();
        if (barcode == null || barcode.isBlank()) {
            filteredList.addAll(productList);
        } else {
            for (Product p : productList) {
                if (p.getBarcode().equalsIgnoreCase(barcode)) {
                    filteredList.add(p);
                    tableView.getSelectionModel().select(p);
                    break;
                }
            }
        }
        tableView.refresh();
    }

    @FXML
    public void handleReadBarcode() {
        barcodeField.requestFocus();
        barcodeField.clear();
    }

    @FXML
    public void handleExitButton() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleUpdateButton() {
        productList.clear();
        loadDataFromDatabase();
        filteredList.clear();
        filteredList.addAll(productList);
        tableView.refresh();
    }
}