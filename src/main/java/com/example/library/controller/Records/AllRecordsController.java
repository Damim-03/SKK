package com.example.library.controller.Records;

import com.example.library.controller.client.rapotsClientController;
import com.example.library.controller.sales.rapotsController;
import com.example.library.model.DebtPayment;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.library.Alert.alert.*;
import static com.example.library.Alert.alert.showAlert;

public class AllRecordsController {
    private static final Logger LOGGER = Logger.getLogger(AllRecordsController.class.getName());
    private static final String SEARCH_HINT_CUSTOMER_ID = "ابحث برقم العميل...";
    private static final String SEARCH_HINT_CUSTOMER_NAME = "ابحث باسم العميل...";
    private static final long SEARCH_DELAY_MS = 300;

    // الجدول الأول ومكوناته
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ObservableList<Sale> salesData = FXCollections.observableArrayList();

    @FXML private Button refreshButton;
    @FXML private TextField customerIdField;
    @FXML private TextField customerNameField;
    @FXML private TableView<Sale> reportTableView;
    @FXML private TableColumn<Sale, Integer> numberColumn;
    @FXML private TableColumn<Sale, String> customerIdColumn;
    @FXML private TableColumn<Sale, String> customerNameColumn;
    @FXML private TableColumn<Sale, Double> subTotalColumn;
    @FXML private TableColumn<Sale, Double> discountColumn;
    @FXML private TableColumn<Sale, Double> debtColumn;
    @FXML private TableColumn<Sale, Double> grandTotalColumn;
    @FXML private TableColumn<Sale, String> dateColumn;
    @FXML private TableColumn<Sale, String> timeColumn;
    @FXML private Button showButton;
    @FXML private Button cancelButton;

    // الجدول الثاني ومكوناته
    private final ScheduledExecutorService executor2 = Executors.newSingleThreadScheduledExecutor();
    private ObservableList<Sale> salesData2 = FXCollections.observableArrayList();

    @FXML private Button refreshButton2;
    @FXML private TextField customerIdField2;
    @FXML private TextField customerNameField2;
    @FXML private TableView<Sale> reportTableView2;
    @FXML private TableColumn<Sale, Integer> numberColumn2;
    @FXML private TableColumn<Sale, String> customerIdColumn2;
    @FXML private TableColumn<Sale, String> customerNameColumn2;
    @FXML private TableColumn<Sale, Double> subTotalColumn2;
    @FXML private TableColumn<Sale, Double> discountColumn2;
    @FXML private TableColumn<Sale, Double> debtColumn2;
    @FXML private TableColumn<Sale, Double> grandTotalColumn2;
    @FXML private TableColumn<Sale, String> dateColumn2;
    @FXML private TableColumn<Sale, String> timeColumn2;
    @FXML private Button showButton2;
    @FXML private Button cancelButton2;

    @FXML private TextField clientIdField;
    @FXML private TextField clientNameField;

    @FXML private TableView<DebtPayment> debtTable;
    @FXML private TableColumn<DebtPayment, Integer> colPaymentId;
    @FXML private TableColumn<DebtPayment, String> colClientId;
    @FXML private TableColumn<DebtPayment, String> colClientName;
    @FXML private TableColumn<DebtPayment, Double> colDebtAmount;
    @FXML private TableColumn<DebtPayment, java.sql.Date> colPaymentDate;
    @FXML private TableColumn<DebtPayment, java.sql.Time> colPaymentTime;
    @FXML private TableColumn<DebtPayment, String> colPaymentStatus;

    private ObservableList<DebtPayment> paymentList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        try {
            // تهيئة الجدول الأول
            setupTableColumns();
            setupButtonActions();
            initializeSearchFields();
            loadSalesData();

            // تهيئة الجدول الثاني
            setupTableColumns2();
            setupButtonActions2();
            initializeSearchFields2();
            loadSalesData2();

            // ربط الأعمدة بالخصائص
            colPaymentId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("paymentId"));
            colClientId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("customerId"));
            colClientName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("customerName"));
            colDebtAmount.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("amountPaid"));
            colPaymentDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("paymentDate"));
            colPaymentTime.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("paymentTime"));
            colPaymentStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

            loadPaymentsFromDatabase();

            // Trigger search when typing
            clientIdField.textProperty().addListener((obs, oldVal, newVal) -> searchPayments());
            clientNameField.textProperty().addListener((obs, oldVal, newVal) -> searchPayments());
        } catch (Exception e) {
            showFailedAlert("خطأ", "خطأ في التهيئة:" + e.getMessage());
            LOGGER.log(Level.SEVERE, "Initialization error", e);
        }
    }

    // ============ طرق الجدول الأول ============
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

        String query = "SELECT * FROM sales WHERE 1=1" +
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
                    "/com/example/interfaces/sales/Form/raports.fxml"));
            Parent root = loader.load();

            rapotsController controller = loader.getController();
            ObservableList<SaleItem> saleItems = loadSaleItems(sale.getSaleId());
            if (saleItems.isEmpty()) {
                showWarningAlert("تحذير", "لا توجد منتجات لهذه الفاتورة (Sale ID: " + sale.getSaleId() + ")");
            }
            controller.setSaleData(sale, saleItems);

            Stage raportStage = new Stage();
            raportStage.setTitle("فاتورة #" + sale.getSaleId());
            raportStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            raportStage.setScene(scene);

            raportStage.showAndWait();

        } catch (IOException e) {
            showFailedAlert("خطأ", "تعذر فتح نافذة الفاتورة: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Failed to open bill window", e);
        } catch (SQLException e) {
            showFailedAlert("خطأ", "تعذر تحميل بيانات الفاتورة: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Failed to load sale items", e);
        }
    }

    private ObservableList<SaleItem> loadSaleItems(int saleId) throws SQLException {
        ObservableList<SaleItem> items = FXCollections.observableArrayList();
        String query = "SELECT number, product_id, product_name, quantity, price, total_price FROM sale_items WHERE sale_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                items.add(new SaleItem(
                        rs.getString("number"),
                        rs.getString("product_id"),
                        rs.getString("product_name"),
                        null,
                        String.valueOf(rs.getInt("quantity")),
                        String.format("%.2f", rs.getDouble("price")),
                        String.format("%.2f", rs.getDouble("total_price"))
                ));
            }
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
                loadSalesData();
                Platform.runLater(() -> showWarningAlert("تحديث", "تم تحديث القائمة"));
            } catch (Exception e) {
                Platform.runLater(() -> showFailedAlert("خطأ", "فشل التحديث"));
                LOGGER.log(Level.SEVERE, "Refresh failed", e);
            } finally {
                Platform.runLater(() -> refreshButton.setDisable(false));
            }
        });
    }

    private void loadSalesData() {
        salesData.clear();
        String query = "SELECT * FROM sales";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) salesData.add(createSaleFromResultSet(rs));
            Platform.runLater(() -> reportTableView.setItems(salesData));
        } catch (SQLException e) {
            showDatabaseError("فشل تحميل البيانات", e);
        }
    }

    private Sale createSaleFromResultSet(ResultSet rs) throws SQLException {
        return new Sale(
                rs.getInt("sale_id"),
                rs.getDate("sale_date").toLocalDate(),
                rs.getTime("sale_time").toLocalTime(),
                rs.getDouble("subtotal"),
                rs.getDouble("discount"),
                rs.getDouble("debt"),
                rs.getDouble("total"),
                rs.getString("customer_name"),
                rs.getString("customer_id")
        );
    }

    private void showDatabaseError(String title, SQLException e) {
        Platform.runLater(() -> showFailedAlert("خطأ في قاعدة البيانات", title + ": " + e.getMessage()));
        LOGGER.log(Level.SEVERE, title, e);
    }

    // ============ طرق الجدول الثاني ============
    private void initializeSearchFields2() {
        customerIdField2.setPromptText(SEARCH_HINT_CUSTOMER_ID);
        customerNameField2.setPromptText(SEARCH_HINT_CUSTOMER_NAME);

        customerIdField2.textProperty().addListener((obs, oldVal, newVal) -> scheduleSearch2());
        customerNameField2.textProperty().addListener((obs, oldVal, newVal) -> scheduleSearch2());
    }

    private void scheduleSearch2() {
        executor2.schedule(this::performSearch2, SEARCH_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void performSearch2() {
        String customerId = customerIdField2.getText().trim();
        String customerName = customerNameField2.getText().trim();
        ObservableList<Sale> filteredData2 = FXCollections.observableArrayList();

        if (customerId.isEmpty() && customerName.isEmpty()) {
            Platform.runLater(() -> reportTableView2.setItems(salesData2));
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
            while (rs.next()) filteredData2.add(createSaleFromResultSet2(rs));

            Platform.runLater(() -> reportTableView2.setItems(filteredData2));
        } catch (SQLException e) {
            Platform.runLater(() -> showDatabaseError2("فشل في البحث", e));
        }
    }

    private void setupTableColumns2() {
        numberColumn2.setCellValueFactory(cell -> cell.getValue().saleIdProperty().asObject());
        customerIdColumn2.setCellValueFactory(cell -> cell.getValue().customerIdProperty());
        customerNameColumn2.setCellValueFactory(cell -> cell.getValue().customerNameProperty());
        subTotalColumn2.setCellValueFactory(cell -> cell.getValue().subtotalProperty().asObject());
        discountColumn2.setCellValueFactory(cell -> cell.getValue().discountProperty().asObject());
        debtColumn2.setCellValueFactory(cell -> cell.getValue().debtProperty().asObject());
        grandTotalColumn2.setCellValueFactory(cell -> cell.getValue().totalProperty().asObject());
        dateColumn2.setCellValueFactory(cell -> cell.getValue().saleDateProperty().asString());
        timeColumn2.setCellValueFactory(cell -> cell.getValue().saleTimeProperty().asString());

        subTotalColumn2.setCellFactory(col -> formatCurrencyCell2());
        discountColumn2.setCellFactory(col -> formatCurrencyCell2());
        debtColumn2.setCellFactory(col -> formatCurrencyCell2());
        grandTotalColumn2.setCellFactory(col -> formatCurrencyCell2());
    }

    private TableCell<Sale, Double> formatCurrencyCell2() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : String.format("%.2f DZ", value));
            }
        };
    }

    private void setupButtonActions2() {
        showButton2.setOnAction(event -> {
            Sale selectedSale = reportTableView2.getSelectionModel().getSelectedItem();
            if (selectedSale != null) {
                openBillWindow2(selectedSale);
            } else {
                showWarningAlert("تحذير", "الرجاء تحديد فاتورة لعرضها");
            }
        });

        cancelButton2.setOnAction(event -> ((Stage) cancelButton2.getScene().getWindow()).close());
    }

    private void openBillWindow2(Sale sale) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/interfaces/sales/Form/raports.fxml"));
            Parent root = loader.load();

            rapotsController controller = loader.getController();
            ObservableList<SaleItem> saleItems = loadSaleItems2(sale.getSaleId());
            if (saleItems.isEmpty()) {
                showWarningAlert("تحذير", "لا توجد منتجات لهذه الفاتورة (Sale ID: " + sale.getSaleId() + ")");
            }
            controller.setSaleData(sale, saleItems);

            Stage raportStage = new Stage();
            raportStage.setTitle("فاتورة #" + sale.getSaleId());
            raportStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            raportStage.setScene(scene);

            raportStage.showAndWait();

        } catch (IOException e) {
            showFailedAlert("خطأ", "تعذر فتح نافذة الفاتورة: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Failed to open bill window", e);
        } catch (SQLException e) {
            showFailedAlert("خطأ", "تعذر تحميل بيانات الفاتورة: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Failed to load sale items", e);
        }
    }

    private ObservableList<SaleItem> loadSaleItems2(int saleId) throws SQLException {
        ObservableList<SaleItem> items = FXCollections.observableArrayList();
        String query = "SELECT number, product_id, product_name, quantity, price, total_price FROM purchase_items WHERE purchase_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                items.add(new SaleItem(
                        rs.getString("number"),
                        rs.getString("product_id"),
                        rs.getString("product_name"),
                        null,
                        String.valueOf(rs.getInt("quantity")),
                        String.format("%.2f", rs.getDouble("price")),
                        String.format("%.2f", rs.getDouble("total_price"))
                ));
            }
        }
        return items;
    }

    @FXML
    private void handleRefresh2() {
        refreshButton2.setDisable(true);
        executor2.submit(() -> {
            try {
                Platform.runLater(() -> {
                    customerIdField2.clear();
                    customerNameField2.clear();
                });
                loadSalesData2();
                Platform.runLater(() -> showWarningAlert("تحديث", "تم تحديث القائمة"));
            } catch (Exception e) {
                Platform.runLater(() -> showFailedAlert("خطأ", "فشل التحديث"));
                LOGGER.log(Level.SEVERE, "Refresh failed", e);
            } finally {
                Platform.runLater(() -> refreshButton2.setDisable(false));
            }
        });
    }

    private void loadSalesData2() {
        salesData2.clear();
        String query = "SELECT * FROM purchases";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) salesData2.add(createSaleFromResultSet2(rs));
            Platform.runLater(() -> reportTableView2.setItems(salesData2));
        } catch (SQLException e) {
            showDatabaseError2("فشل تحميل البيانات", e);
        }
    }

    private Sale createSaleFromResultSet2(ResultSet rs) throws SQLException {
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

    private void showDatabaseError2(String title, SQLException e) {
        Platform.runLater(() -> showFailedAlert("خطأ في قاعدة البيانات", title + ": " + e.getMessage()));
        LOGGER.log(Level.SEVERE, title, e);
    }

    private void loadPaymentsFromDatabase() {
        paymentList.clear();

        String sql = "SELECT payment_id, customer_id, customer_name, amount_paid, payment_date, payment_time, status FROM client_payments";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                paymentList.add(new DebtPayment(
                        rs.getInt("payment_id"),
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getDouble("amount_paid"),
                        rs.getDate("payment_date"),
                        rs.getTime("payment_time"),
                        rs.getString("status")
                ));
            }

            debtTable.setItems(paymentList);

        } catch (SQLException e) {
            showError("خطأ في الاتصال بقاعدة البيانات", e.getMessage());
        }
    }

    private void searchPayments() {
        paymentList.clear();

        String sql = "SELECT payment_id, customer_id, customer_name, amount_paid, payment_date, payment_time, status " +
                "FROM client_payments WHERE 1=1 ";

        if (!clientIdField.getText().trim().isEmpty()) {
            sql += " AND customer_id LIKE ? ";
        }
        if (!clientNameField.getText().trim().isEmpty()) {
            sql += " AND customer_name LIKE ? ";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (!clientIdField.getText().trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + clientIdField.getText().trim() + "%");
            }
            if (!clientNameField.getText().trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + clientNameField.getText().trim() + "%");
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                paymentList.add(new DebtPayment(
                        rs.getInt("payment_id"),
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getDouble("amount_paid"),
                        rs.getDate("payment_date"),
                        rs.getTime("payment_time"),
                        rs.getString("status")
                ));
            }

            debtTable.setItems(paymentList);

        } catch (SQLException e) {
            showError("خطأ في البحث", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        // Close the current window
        Stage stage = (Stage) clientIdField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleRefresh3() {
        loadPaymentsFromDatabase();
    }

    @FXML
    private void handleSaveAsExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("حفظ كملف Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("client_payments.xlsx");

        File file = fileChooser.showSaveDialog(debtTable.getScene().getWindow());
        if (file != null) {
            saveTableToExcel(file);
        }
    }

    private void saveTableToExcel(File file) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Client Payments");

            // إنشاء رأس الجدول
            Row header = sheet.createRow(0);
            String[] headers = {"رقم الدفع", "رقم العميل", "اسم العميل", "المبلغ المدفوع", "تاريخ الدفع", "وقت الدفع", "حالة الدفع"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // إنشاء تنسيق السعر 0.00 DZ
            CellStyle priceStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            priceStyle.setDataFormat(format.getFormat("0.00 \"DZ\""));

            // إضافة البيانات
            ObservableList<DebtPayment> data = debtTable.getItems();
            for (int i = 0; i < data.size(); i++) {
                DebtPayment dp = data.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(dp.getPaymentId());
                row.createCell(1).setCellValue(dp.getCustomerId());
                row.createCell(2).setCellValue(dp.getCustomerName());

                // المبلغ مع التنسيق
                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(dp.getAmountPaid());
                amountCell.setCellStyle(priceStyle);

                row.createCell(4).setCellValue(dp.getPaymentDate().toString());
                row.createCell(5).setCellValue(dp.getPaymentTime().toString());
                row.createCell(6).setCellValue(dp.getStatus());
            }

            // توسيع الأعمدة
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // حفظ الملف
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "تم حفظ الملف بنجاح!", ButtonType.OK);
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "فشل حفظ الملف: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}