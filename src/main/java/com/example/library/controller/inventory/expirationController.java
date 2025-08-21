package com.example.library.controller.inventory;

import com.example.library.model.Product;
import com.example.library.util.DatabaseConnection;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.awt.Toolkit;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.example.library.Alert.alert.showFailedAlert;
import static com.example.library.Alert.alert.showWarningAlert;

public class expirationController {

    @FXML private TextField barcodeField;
    @FXML private Button ReadBarcode;
    @FXML private TableView<Product> tableView;
    @FXML private TableColumn<Product, Boolean> selectColumn;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, LocalDate> productionDateColumn;
    @FXML private TableColumn<Product, LocalDate> expiryDateColumn;
    @FXML private TableColumn<Product, String> expiryDateColumn1; // Status column


    @FXML private Button exitButton;
    @FXML private Button updateListButton;
    @FXML private Button infoProductButton;

    private ObservableList<Product> productData = FXCollections.observableArrayList();
    private ObservableList<Product> filteredProductData = FXCollections.observableArrayList();
    private PauseTransition barcodePause;

    @FXML
    public void initialize() {
        setupTableColumns();
        configureTableRowFactory();
        loadProductData();
        setupBarcodeHandling();
    }

    private void setupTableColumns() {
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));

        productionDateColumn.setCellValueFactory(new PropertyValueFactory<>("productionDate"));
        productionDateColumn.setCellFactory(column -> new TableCell<Product, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? "" : date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        });

        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        expiryDateColumn.setCellFactory(column -> new TableCell<Product, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? "" : date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        });
        expiryDateColumn1.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            LocalDate expiryDate = product.getExpiryDate();
            String status = "Valid";
            if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
                status = "Expired";
            }
            return new javafx.beans.property.SimpleStringProperty(status);
        });

    }

    private void configureTableRowFactory() {
        tableView.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (product == null || empty) {
                    setStyle("");
                    return;
                }

                LocalDate today = LocalDate.now();
                LocalDate expiryDate = product.getExpiryDate();

                if (expiryDate == null) {
                    setStyle("");
                } else if (expiryDate.isBefore(today)) {
                    setStyle("-fx-background-color: #ffdddd;");
                } else {
                    setStyle("-fx-background-color: #ddffdd;");
                }
            }
        });
    }

    private void setupBarcodeHandling() {
        barcodePause = new PauseTransition(Duration.millis(150));
        barcodePause.setOnFinished(event -> processScannedBarcode());

        barcodeField.setOnMouseClicked(e -> {
            barcodeField.clear();
            showAllProducts();
        });

        barcodeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > oldVal.length()) {
                barcodePause.playFromStart();
            } else if (newVal.isEmpty()) {
                showAllProducts();
            }
        });

        barcodeField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                barcodePause.stop();
                processScannedBarcode();
            }
        });
    }

    private void processScannedBarcode() {
        String scannedBarcode = barcodeField.getText().trim();
        if (!scannedBarcode.isEmpty()) {
            String cleanBarcode = scannedBarcode.replaceAll("[^a-zA-Z0-9]", "");
            if (cleanBarcode.isEmpty()) {
                showWarningAlert("Barcode", "البار كود المدخل خاطى.");
                return;
            }

            boolean found = searchInLoadedProducts(cleanBarcode);
            if (!found) {
                searchInDatabase(cleanBarcode);
            }
        }
    }

    private boolean searchInLoadedProducts(String barcode) {
        filteredProductData.clear();
        for (Product product : productData) {
            if (product.getBarcode().equals(barcode)) {
                filteredProductData.add(product);
                tableView.setItems(filteredProductData);
                highlightProductInTable(product);
                return true;
            }
        }
        return false;
    }

    private void searchInDatabase(String barcode) {
        String query = "SELECT barcode, product_name, production_date, expiration_date FROM products WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Product product = new Product();
                product.setBarcode(rs.getString("barcode"));
                product.setProductName(rs.getString("product_name"));

                Date prodDate = rs.getDate("production_date");
                product.setProductionDate(prodDate != null ? prodDate.toLocalDate() : null);

                Date expDate = rs.getDate("expiration_date");
                product.setExpiryDate(expDate != null ? expDate.toLocalDate() : null);

                Platform.runLater(() -> {
                    productData.add(product);
                    filteredProductData.clear();
                    filteredProductData.add(product);
                    tableView.setItems(filteredProductData);
                    highlightProductInTable(product);
                });
            } else {
                showFailedAlert("فشل", "لم يتم عثور على المنتج :" + barcode);
                showAllProducts();
            }
        } catch (SQLException e) {
            showFailedAlert("خطأ في قاعدة البيانات", "لم يتم عثور على المنتج.");
        }
    }

    private void showAllProducts() {
        filteredProductData.setAll(productData);
        tableView.setItems(filteredProductData);
    }

    private void highlightProductInTable(Product product) {
        Platform.runLater(() -> {
            tableView.getSelectionModel().select(product);
            tableView.scrollTo(product);
            Toolkit.getDefaultToolkit().beep();

            TableRow<Product> row = (TableRow<Product>) tableView.lookup(".table-row-cell:selected");
            if (row != null) {
                row.setStyle("-fx-background-color: #ffff99;");
                PauseTransition flash = new PauseTransition(Duration.millis(300));
                flash.setOnFinished(e -> {
                    LocalDate today = LocalDate.now();
                    if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(today)) {
                        row.setStyle("-fx-background-color: #ffdddd;");
                    } else {
                        row.setStyle("-fx-background-color: #ddffdd;");
                    }
                });
                flash.play();
            }
        });
    }

    private void loadProductData() {
        productData.clear();
        String query = "SELECT barcode, product_name, production_date, expiration_date FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Product product = new Product();
                product.setBarcode(rs.getString("barcode"));
                product.setProductName(rs.getString("product_name"));

                Date prodDate = rs.getDate("production_date");
                product.setProductionDate(prodDate != null ? prodDate.toLocalDate() : null);

                Date expDate = rs.getDate("expiration_date");
                product.setExpiryDate(expDate != null ? expDate.toLocalDate() : null);

                productData.add(product);
            }

            filteredProductData.setAll(productData);
            tableView.setItems(filteredProductData);
        } catch (SQLException e) {
            showFailedAlert("خطأ في قاعدة البيانات", "مشكلة في تحميل المنتجات");
        }
    }

    @FXML
    private void handleReadBarcode() {
        barcodeField.requestFocus(); // Focus on the barcode field
        processScannedBarcode();     // Then handle the scan logic
    }


    @FXML
    private void handleExitButton() {
        exitButton.getScene().getWindow().hide();
    }

    @FXML
    private void handleUpdateButton() {
        loadProductData();
    }

    @FXML
    private void handleInfoButton() {
        Product selectedProduct = tableView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showWarningAlert("تنبيه", "يرجى اختيار منتج من القائمة أولاً");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/inventory/Form/productForm.fxml"));
            Parent root = loader.load();

            // Get the controller and pass barcode
            productShowController controller = loader.getController();
            controller.setProductData(selectedProduct.getBarcode());

            Stage newStage = new Stage();
            newStage.setTitle("معلومات المنتج");
            newStage.setScene(new Scene(root));
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/date.png"))));
            newStage.setResizable(false);
            newStage.setMaximized(false);
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "فشل في اظهار نافذة معلومات المنتج");
        }
    }
}
