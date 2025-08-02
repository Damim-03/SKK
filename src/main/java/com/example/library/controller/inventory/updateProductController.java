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
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class updateProductController {

    @FXML public Button ReadBarcode;
    @FXML public Button exitButton;
    @FXML public Button updateListButton;
    @FXML public TextField barcodeTextField;
    @FXML public Button readButton;
    @FXML private TableView<Product> tableView;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Double> priceColumn1;
    @FXML private TableColumn<Product, Double> priceColumn2;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, Boolean> selectColumn;

    @FXML private TextField barcodeField;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<Product> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up TableView columns with minimum widths and custom cell factories
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price1"));
        priceColumn1.setCellValueFactory(new PropertyValueFactory<>("price2"));
        priceColumn2.setCellValueFactory(new PropertyValueFactory<>("price3"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        barcodeColumn.setMinWidth(120);
        nameColumn.setMinWidth(130);
        priceColumn.setMinWidth(110);
        quantityColumn.setMinWidth(120);
        selectColumn.setMinWidth(40);

        // Custom cell factory for priceColumn to format Double
        priceColumn.setCellFactory(new Callback<TableColumn<Product, Double>, TableCell<Product, Double>>() {
            @Override
            public TableCell<Product, Double> call(TableColumn<Product, Double> param) {
                return new TableCell<Product, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(String.format("%.2f", item));
                        }
                    }
                };
            }
        });

        // Load all data
        loadDataFromDatabase();
        filteredList.addAll(productList); // Initialize filteredList with all products
        tableView.setItems(filteredList);
        tableView.refresh();

        // Add listener to barcodeField to filter table
        barcodeField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTableByBarcode(newValue);
        });

        // Update fields when a row is selected
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                barcodeField.setText(newSelection.getBarcode());
            }
        });
    }

    private Image loadImage(String path) {
        if (path.startsWith("/") || !path.contains(":")) {
            // Treat as classpath resource
            try {
                return new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            } catch (Exception e) {
                System.out.println("Resource not found or error for path: " + path + " - " + e.getMessage());
                return null;
            }
        } else {
            // Treat as file system path
            File file = new File(path);
            if (file.exists()) {
                try {
                    return new Image(file.toURI().toString());
                } catch (Exception e) {
                    System.out.println("File load error for path: " + path + " - " + e.getMessage());
                    return null;
                }
            } else {
                System.out.println("File does not exist: " + path);
                return null;
            }
        }
    }

    private void filterTableByBarcode(String barcode) {
        filteredList.clear();
        if (barcode == null || barcode.trim().isEmpty()) {
            filteredList.addAll(productList); // Show all products if barcode is empty
        } else {
            Product matchingProduct = productList.stream()
                    .filter(p -> p.getBarcode() != null && p.getBarcode().equals(barcode.trim()))
                    .findFirst()
                    .orElse(null);
            if (matchingProduct != null) {
                filteredList.add(matchingProduct);
                // Select the matching product if found
                tableView.getSelectionModel().clearAndSelect(0);
            } else {
                // Clear selection when no match is found
                tableView.getSelectionModel().clearSelection();
            }
        }
        tableView.refresh();
    }

    private void loadDataFromDatabase() {
        String query = "SELECT barcode, product_name, price1, price2, price3, quantity, unit, production_date, expiration_date, image_path FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                productList.add(new Product(
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
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database error: " + e.getMessage());
        }
    }

    @FXML
    public void handleReadBarcode() {
        // Focus the barcodeField to allow scanning
        barcodeField.requestFocus();
        barcodeField.clear();
    }

    @FXML
    public void handleExitButton() {
        try {
            Stage stage = (Stage) exitButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleUpdateButton() {
        try {
            productList.clear(); // Clear existing data
            loadDataFromDatabase(); // Reload data from database
            filteredList.clear();
            filteredList.addAll(productList); // Update filtered list
            tableView.refresh(); // Refresh the table view
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error updating list: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateProductButton(){
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/inventory/updateForm.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("تحديث المنتج");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/cycle.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error opening add new product window: " + e.getMessage());
        }
    }

}