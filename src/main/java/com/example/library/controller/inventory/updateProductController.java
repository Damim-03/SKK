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

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class updateProductController {

    @FXML private Button exitButton;
    @FXML private TextField barcodeField;

    @FXML private TableView<Product> tableView;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Double> priceColumn1;
    @FXML private TableColumn<Product, Double> priceColumn2;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Boolean> selectColumn;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<Product> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadDataFromDatabase();
        setupListeners();
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void setupTableColumns() {
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price1"));
        priceColumn1.setCellValueFactory(new PropertyValueFactory<>("price2"));
        priceColumn2.setCellValueFactory(new PropertyValueFactory<>("price3"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
    }

    private void setupListeners() {
        barcodeField.textProperty().addListener((obs, oldVal, newVal) -> filterTableByBarcode(newVal));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                barcodeField.setText(newSel.getBarcode());
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
                LocalDate prodDate = rs.getDate("production_date") != null ? rs.getDate("production_date").toLocalDate() : null;
                LocalDate expDate = rs.getDate("expiration_date") != null ? rs.getDate("expiration_date").toLocalDate() : null;

                productList.add(new Product(
                        rs.getString("barcode"),
                        rs.getString("product_name"),
                        rs.getString("description"),
                        rs.getDouble("price1"),
                        rs.getDouble("price2"),
                        rs.getDouble("price3"),
                        rs.getInt("quantity"),
                        rs.getString("unit"),
                        rs.getString("category"),
                        prodDate,
                        expDate,
                        rs.getString("image_path")
                ));
            }

            filteredList.setAll(productList);
            tableView.setItems(filteredList);
            tableView.refresh();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load products:\n" + e.getMessage());
        }
    }

    private void filterTableByBarcode(String barcode) {
        filteredList.clear();

        if (barcode == null || barcode.trim().isEmpty()) {
            filteredList.addAll(productList);
        } else {
            productList.stream()
                    .filter(p -> p.getBarcode() != null && p.getBarcode().equalsIgnoreCase(barcode.trim()))
                    .findFirst()
                    .ifPresent(filteredList::add);
        }

        tableView.refresh();
    }

    @FXML
    public void handleReadBarcode() {
        barcodeField.clear();
    }

    @FXML
    public void handleExitButton() {
        ((Stage) exitButton.getScene().getWindow()).close();
    }

    @FXML
    public void handleUpdateButton() {
        loadDataFromDatabase();
        filteredList.setAll(productList);
        tableView.refresh();
    }

    @FXML
    private void handleDeleteProductButton() {
        ObservableList<Product> selectedItems = tableView.getSelectionModel().getSelectedItems();

        if (selectedItems == null || selectedItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "تحذير", "يرجى تحديد صفوف للحذف.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setHeaderText("هل تريد حذف المنتجات المحددة؟");
        confirm.setContentText("عدد الصفوف المحددة: " + selectedItems.size());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                List<Product> toDelete = new ArrayList<>(selectedItems);
                for (Product product : toDelete) {
                    deleteProductFromDatabase(product.getBarcode());
                    productList.remove(product); // ✅ Only remove from master list
                }
                showAlert(Alert.AlertType.INFORMATION, "تم الحذف", "تم حذف الصفوف المحددة.");
            }
        });
    }

    private void deleteProductFromDatabase(String barcode) {
        String query = "DELETE FROM products WHERE barcode = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, barcode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleUpdateProductButton() {
        Product selectedProduct = tableView.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a product to update.");
            return;
        }

        try {
            URL fxmlLocation = getClass().getResource("/com/example/interfaces/inventory/Form/updateForm.fxml");
            if (fxmlLocation == null) throw new IOException("FXML file not found.");

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            updateFormController controller = loader.getController();
            controller.setProductData(selectedProduct);

            Stage stage = new Stage();
            stage.setTitle("Update Product");
            stage.setScene(new Scene(root));
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/cycle.png")));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("Icon not loaded.");
            }

            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open update form:\n" + e.getMessage());
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
