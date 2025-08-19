package com.example.library.controller.client;

import com.example.library.model.SaleItem;
import com.example.library.util.DatabaseConnection;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.example.library.Alert.alert.*;

public class billClientController {

    // Header Section
    @FXML private Label arabicTitleLabel;
    @FXML private Label englishTitleLabel;
    @FXML private Label arabicAddressLabel;
    @FXML private Label englishAddressLabel;

    // Customer Information
    @FXML private Label customerNameLabel;
    @FXML private TextField customerNameField;
    @FXML private TextField customerIdField;

    // Date/Time
    @FXML private Label issueDateLabel;
    @FXML private TextField issueDateField;
    @FXML private Label issueTimeLabel;
    @FXML private TextField issueTimeField;

    // Items Table
    @FXML private TableView<SaleItem> itemsTableView;
    @FXML private TableColumn<SaleItem, String> idColumn;
    @FXML private TableColumn<SaleItem, String> productIdColumn;
    @FXML private TableColumn<SaleItem, String> productNameColumn;
    @FXML private TableColumn<SaleItem, String> quantityColumn;
    @FXML private TableColumn<SaleItem, String> priceColumn;
    @FXML private TableColumn<SaleItem, String> totalPriceColumn;

    // Totals Section
    @FXML private Label subtotalLabel;
    @FXML private TextField subtotalField;
    @FXML private Label discountLabel;
    @FXML private TextField discountField;
    @FXML private Label debtLabel;
    @FXML private TextField debtField;
    @FXML private Label totalLabel;
    @FXML private TextField totalField;

    // Footer Section
    @FXML private Label returnPolicyLabel;
    @FXML private Label returnDeadlineLabel;
    @FXML private Label copyrightLabel;

    @FXML
    public void initialize() {
        setupTableColumns();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // Format currency columns
        priceColumn.setCellFactory(col -> formatCurrencyCell());
        totalPriceColumn.setCellFactory(col -> formatCurrencyCell());
    }

    public void setSalesData(ObservableList<SaleItem> items, String subtotal, String discount,
                             String debt, String total, String date, String time,
                             String customerName, String customerId) {
        itemsTableView.setItems(items);

        // Format currency values
        subtotalField.setText(formatCurrency(subtotal));
        discountField.setText(formatCurrency(discount));
        debtField.setText(formatCurrency(debt));
        totalField.setText(formatCurrency(total));

        // Set date/time (use current if not provided)
        issueDateField.setText(date != null ? date : LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
        issueTimeField.setText(time != null ? time : LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        // Set customer info
        customerNameField.setText(customerName != null ? customerName : "غير محدد");
        customerIdField.setText(customerId != null ? customerId : "غير محدد");
    }

    private String formatCurrency(String value) {
        try {
            double amount = Double.parseDouble(value);
            return String.format("%.2f DZ", amount);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private TableCell<SaleItem, String> formatCurrencyCell() {
        return new TableCell<SaleItem, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    try {
                        double amount = Double.parseDouble(value);
                        setText(String.format("%.2f DZ", amount));
                    } catch (NumberFormatException e) {
                        setText(value);
                    }
                }
            }
        };
    }

    public void saveClientSale(ObservableList<SaleItem> productList,
                               String subtotal,
                               String discount,
                               String debt,
                               String total,
                               String date,
                               String time,
                               String customerName,
                               String customerId) {

        String insertSaleSQL = "INSERT INTO client_sales (sale_date, sale_time, subtotal, discount, debt, total, customer_name, customer_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertItemSQL = "INSERT INTO client_sales_item (sale_id, product_id, number, product_name, quantity, price, total_price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            try (PreparedStatement saleStmt = conn.prepareStatement(insertSaleSQL, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement itemStmt = conn.prepareStatement(insertItemSQL)) {

                conn.setAutoCommit(false); // Start transaction

                // Clean and validate input
                double subtotalValue = parseDoubleSafely(subtotal.replace(" DZ", "").trim());
                double discountValue = parseDoubleSafely(discount.replace(" DZ", "").trim());
                double debtValue = parseDoubleSafely(debt.replace(" DZ", "").trim());
                double totalValue = parseDoubleSafely(total.replace(" DZ", "").trim());

                if (subtotalValue < 0 || discountValue < 0 || debtValue < 0 || totalValue < 0) {
                    showWarningAlert("تنبيه", "لا يُسمح بالقيم السلبية للمبالغ المالية.");
                }

                // 1️⃣ Insert into client_sales
                saleStmt.setDate(1, Date.valueOf(date)); // Ensure date is in yyyy-MM-dd format
                saleStmt.setTime(2, Time.valueOf(time)); // Ensure time is in HH:mm:ss format
                saleStmt.setBigDecimal(3, BigDecimal.valueOf(subtotalValue));
                saleStmt.setBigDecimal(4, BigDecimal.valueOf(discountValue));
                saleStmt.setBigDecimal(5, BigDecimal.valueOf(debtValue));
                saleStmt.setBigDecimal(6, BigDecimal.valueOf(totalValue));
                saleStmt.setString(7, customerName);
                saleStmt.setString(8, customerId);
                int rowsAffected = saleStmt.executeUpdate();
                if (rowsAffected == 0) {
                    showWarningAlert("تنبيه", "لم يتم إدراج أي صفوف .");
                }

                // Get generated sale_id
                int saleId;
                try (ResultSet generatedKeys = saleStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        saleId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Failed to get generated sale_id.");
                    }
                }

                // 2️⃣ Insert each product into client_sales_item
                for (SaleItem item : productList) {
                    // Debug: Log item values
                    itemStmt.setInt(1, saleId);
                    itemStmt.setString(2, item.getProductId());
                    itemStmt.setString(3, item.getNumber());
                    itemStmt.setString(4, item.getProductName());
                    itemStmt.setInt(5, parseIntSafely(item.getQuantity())); // Use safe parsing
                    itemStmt.setBigDecimal(6, new BigDecimal(parseDoubleSafely(item.getPrice())));
                    itemStmt.setBigDecimal(7, new BigDecimal(parseDoubleSafely(item.getTotalPrice())));
                    itemStmt.addBatch();
                }

                itemStmt.executeBatch();
                conn.commit(); // Commit transaction

                showSuccessAlert("نجاح", "تم حفظ الفاتورة");

            }
        } catch (SQLException e) {
            showFailedAlert("خطأ", "خطأ في حفظ!");
            if (conn != null) {
                try {
                    conn.rollback(); // Roll back transaction on error
                } catch (SQLException ex) {
                    showFailedAlert("خطأ", "خطأ في حفظ!");
                }
            }
        } catch (IllegalArgumentException e) {
            showFailedAlert("خطأ", "بيانات غير صالحة: " + e.getMessage());
        } catch (Exception e) {
            showFailedAlert("خطأ", "خطأ غير متوقع: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException e) {
                    showFailedAlert("خطأ", "خطأ في حفظ!");
                }
            }
        }
    }

    // Helper method to parse double safely
    private double parseDoubleSafely(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Helper method to parse int safely
    private int parseIntSafely(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
