package com.example.library.controller.inventory;

import com.example.library.model.Product;
import com.example.library.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
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
        setupTableColumns();
        loadDataFromDatabase();
        setupListeners();
    }

    private void setupTableColumns() {
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

        priceColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Product, Double> call(TableColumn<Product, Double> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : String.format("%.2f", item));
                    }
                };
            }
        });
    }

    private void setupListeners() {
        barcodeField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTableByBarcode(newValue);
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                barcodeField.setText(newSelection.getBarcode());
            }
        });
    }

    private void loadDataFromDatabase() {
        String query = "SELECT barcode, product_name, description, price1, price2, price3, " +
                "quantity, unit, production_date, expiration_date, image_path FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            productList.clear();

            while (rs.next()) {
                LocalDate productionDate = rs.getDate("production_date") != null ?
                        rs.getDate("production_date").toLocalDate() : null;
                LocalDate expirationDate = rs.getDate("expiration_date") != null ?
                        rs.getDate("expiration_date").toLocalDate() : null;

                productList.add(new Product(
                        rs.getString("barcode"),
                        rs.getString("product_name"),
                        rs.getString("description"),
                        rs.getDouble("price1"),
                        rs.getDouble("price2"),
                        rs.getDouble("price3"),
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        productionDate,
                        expirationDate,
                        rs.getString("image_path")
                ));
            }

            filteredList.setAll(productList);
            tableView.setItems(filteredList);

            if (filteredList.isEmpty()) {
                tableView.getSelectionModel().clearSelection();
            }

            tableView.refresh();

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load products: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void filterTableByBarcode(String barcode) {
        filteredList.clear();
        if (barcode == null || barcode.trim().isEmpty()) {
            filteredList.addAll(productList);
        } else {
            Product matchingProduct = productList.stream()
                    .filter(p -> p.getBarcode() != null && p.getBarcode().equals(barcode.trim()))
                    .findFirst()
                    .orElse(null);

            if (matchingProduct != null) {
                filteredList.add(matchingProduct);
                if (!filteredList.isEmpty()) {
                    tableView.getSelectionModel().clearAndSelect(0);
                }
            } else {
                tableView.getSelectionModel().clearSelection();
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
        ((Stage) exitButton.getScene().getWindow()).close();
    }

    @FXML
    public void handleUpdateButton() {
        productList.clear();
        loadDataFromDatabase();
        filteredList.clear();
        filteredList.addAll(productList);
        tableView.refresh();
    }

    @FXML
    public void handleUpdateProductButton() {
        Product selectedProduct = tableView.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert("No Selection", "Please select a product to update", Alert.AlertType.WARNING);
            return;
        }

        try {
            URL fxmlLocation = getClass().getResource("/com/example/interfaces/inventory/updateForm.fxml");
            if (fxmlLocation == null) {
                throw new IOException("FXML file not found at the specified location");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            updateFormController controller = loader.getController();
            controller.setProductData(selectedProduct);

            Stage stage = new Stage();
            stage.setTitle("تحديث المنتج");
            stage.setScene(new Scene(root));

            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/cycle.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("Could not load window icon");
            }

            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            showAlert("Error", "Failed to open update form:\n" + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}