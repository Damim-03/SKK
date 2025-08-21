package com.example.library.controller.Purchases;

import com.example.library.controller.sales.getRaportsController;
import com.example.library.model.Sale;
import com.example.library.model.SaleItem;
import com.example.library.util.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.library.Alert.alert.*;

public class getRaportsPurchasesController {

    private static final Logger LOGGER = Logger.getLogger(getRaportsController.class.getName());
    private static final String SEARCH_HINT_CUSTOMER_ID = "ابحث برقم العميل...";
    private static final String SEARCH_HINT_CUSTOMER_NAME = "ابحث باسم العميل...";
    private static final long SEARCH_DELAY_MS = 300;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ObservableList<Sale> salesData = FXCollections.observableArrayList();

    @FXML
    private Button refreshButton;
    @FXML private TextField customerIdField;
    @FXML private TextField customerNameField;
    @FXML private TableView<Sale> reportTableView;
    @FXML private TableColumn<Sale, Integer> numberColumn;
    @FXML private TableColumn<Sale, String> customerIdColumn;
    @FXML private TableColumn<Sale, String> customerNameColumn;
    @FXML private TableColumn<Sale, Double> subTotalColumn; // match FXML ID
    @FXML private TableColumn<Sale, Double> discountColumn;
    @FXML private TableColumn<Sale, Double> debtColumn;
    @FXML private TableColumn<Sale, Double> grandTotalColumn; // match FXML ID
    @FXML private TableColumn<Sale, String> dateColumn;
    @FXML private TableColumn<Sale, String> timeColumn;
    @FXML private Button showButton;
    @FXML private Button cancelButton;

    @FXML
    private void initialize() {
        try {
            setupTableColumns();
            setupButtonActions();
            initializeSearchFields();
            loadPurchaseData();
        } catch (Exception e) {
            showFailedAlert("فشل في التهيئة", "فشل في تهيئة.");
            LOGGER.log(Level.SEVERE, "Initialization Error", e);
        }
    }

    private void initializeSearchFields() {
        customerIdField.setPromptText(SEARCH_HINT_CUSTOMER_ID);
        customerNameField.setPromptText(SEARCH_HINT_CUSTOMER_NAME);

        customerIdField.textProperty().addListener((obs, oldVal, newVal) -> scheduleSearch());
        customerNameField.textProperty().addListener((obs, oldVal, newVal) -> scheduleSearch());
    }

    private void scheduleSearch() {
        executor.schedule(this::performSearch, SEARCH_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void performSearch() {
        String customerId = customerIdField.getText().trim();
        String customerName = customerNameField.getText().trim();
        ObservableList<Sale> filteredData = FXCollections.observableArrayList();

        if (customerId.isEmpty() && customerName.isEmpty()) {
            Platform.runLater(() -> reportTableView.setItems(salesData));
            return;
        }

        String query = "SELECT * FROM purchases WHERE 1=1" +
                (!customerId.isEmpty() ? " AND customer_id LIKE ?" : "") +
                (!customerName.isEmpty() ? " AND customer_name LIKE ?": "");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            int paramIndex = 1;
            if (!customerId.isEmpty()) pstmt.setString(paramIndex++, "%" + customerId + "%");
            if (!customerName.isEmpty()) pstmt.setString(paramIndex++, "%" + customerName + "%");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) filteredData.add(createSaleFromResultSet(rs));

            Platform.runLater(() -> reportTableView.setItems(filteredData));
        } catch (SQLException e) {
            Platform.runLater(() -> showDatabaseError("فشل في البحث", e));
        }
    }

    private void setupTableColumns() {
        numberColumn.setCellValueFactory(cell -> cell.getValue().saleIdProperty().asObject());
        customerIdColumn.setCellValueFactory(cell -> cell.getValue().customerIdProperty());
        customerNameColumn.setCellValueFactory(cell -> cell.getValue().customerNameProperty());
        subTotalColumn.setCellValueFactory(cell -> cell.getValue().subtotalProperty().asObject());
        discountColumn.setCellValueFactory(cell -> cell.getValue().discountProperty().asObject());
        debtColumn.setCellValueFactory(cell -> cell.getValue().debtProperty().asObject());
        grandTotalColumn.setCellValueFactory(cell -> cell.getValue().totalProperty().asObject());
        dateColumn.setCellValueFactory(cell -> cell.getValue().saleDateProperty().asString());
        timeColumn.setCellValueFactory(cell -> cell.getValue().saleTimeProperty().asString());

        subTotalColumn.setCellFactory(col -> formatCurrencyCell());
        discountColumn.setCellFactory(col -> formatCurrencyCell());
        debtColumn.setCellFactory(col -> formatCurrencyCell());
        grandTotalColumn.setCellFactory(col -> formatCurrencyCell());
    }

    private TableCell<Sale, Double> formatCurrencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : String.format("%.2f DZ", value));
            }
        };
    }

    private void setupButtonActions() {
        showButton.setOnAction(event -> {
            Sale selectedSale = reportTableView.getSelectionModel().getSelectedItem();
            if (selectedSale != null) {
                openBillWindow(selectedSale);
            } else {
                showWarningAlert("تحذير", "الرجاء تحديد فاتورة لعرضها");
            }
        });

        cancelButton.setOnAction(event -> ((Stage) cancelButton.getScene().getWindow()).close());
    }

    private void openBillWindow(Sale sale) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/interfaces/purchases/Form/raportspurchase.fxml"));
            Parent root = loader.load();

            raportsPurchaseController controller = loader.getController();
            ObservableList<SaleItem> saleItems = loadPurchaseItems(sale.getSaleId());
            if (saleItems.isEmpty()) {
                showWarningAlert("تحذير", "لا توجد منتجات لهذه الفاتورة (Sale ID: " + sale.getSaleId() + ")");
            }
            controller.setPurchaseData(sale, saleItems);

            Stage raportStage = new Stage();
            raportStage.setTitle("فاتورة #" + sale.getSaleId());
            raportStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            raportStage.setScene(scene);

            raportStage.showAndWait();

        } catch (IOException e) {
            showFailedAlert("خطأ", "تعذر فتح نافذة الفاتورة.");
        } catch (SQLException e) {
            showFailedAlert("خطأ", "تعذر تحميل بيانات الفاتورة.");
        }
    }

    private ObservableList<SaleItem> loadPurchaseItems(int saleId) throws SQLException {
        ObservableList<SaleItem> items = FXCollections.observableArrayList();
        String query = "SELECT number, product_id, product_name, quantity, price, total_price FROM purchase_items WHERE purchase_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                items.add(new SaleItem(
                        rs.getString("number"),              // number
                        rs.getString("product_id"),          // product_id
                        rs.getString("product_name"),        // product_name
                        null,                                // barcode (set to null if not in query)
                        String.valueOf(rs.getInt("quantity")), // quantity as String
                        String.format("%.2f", rs.getDouble("price")), // price as formatted String
                        String.format("%.2f", rs.getDouble("total_price")) // total_price as formatted String
                ));
            }
            showSuccessAlert("نجاح" , "تم التحميل");
        } catch (SQLException e) {
            showFailedAlert("فشل" , "فشل في التحميل");
            throw e; // Re-throw to be handled by the caller
        }
        return items;
    }

    @FXML
    private void handleRefresh() {
        refreshButton.setDisable(true);
        executor.submit(() -> {
            try {
                Platform.runLater(() -> {
                    customerIdField.clear();
                    customerNameField.clear();
                });
                loadPurchaseData();
                Platform.runLater(() -> showSuccessAlert("تحديث", "تم تحديث القائمة"));
            } catch (Exception e) {
                Platform.runLater(() -> showFailedAlert("خطأ", "فشل التحديث"));
                LOGGER.log(Level.SEVERE, "Refresh failed", e);
            } finally {
                Platform.runLater(() -> refreshButton.setDisable(false));
            }
        });
    }

    private void loadPurchaseData() {
        salesData.clear();
        String query = "SELECT * FROM purchases";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) salesData.add(createSaleFromResultSet(rs));
            reportTableView.setItems(salesData);
        } catch (SQLException e) {
            showDatabaseError("فشل تحميل البيانات", e);
        }
    }

    private Sale createSaleFromResultSet(ResultSet rs) throws SQLException {
        return new Sale(
                rs.getInt("purchase_id"),
                rs.getDate("purchase_date").toLocalDate(),
                rs.getTime("purchase_time").toLocalTime(),
                rs.getDouble("subtotal"),
                rs.getDouble("discount"),
                rs.getDouble("debt"),
                rs.getDouble("total"),
                rs.getString("customer_name"),
                rs.getString("customer_id")
        );
    }

    private void showDatabaseError(String title, SQLException e) {
        showFailedAlert("خطأ في قاعدة البيانات", "خطا في قاعد البيانات");
        LOGGER.log(Level.SEVERE, title, e);
    }
}
