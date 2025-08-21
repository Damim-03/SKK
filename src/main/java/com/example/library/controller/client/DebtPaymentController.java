package com.example.library.controller.client;

import com.example.library.model.DebtPayment;
import com.example.library.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.library.Alert.alert.showFailedAlert;
import static com.example.library.Alert.alert.showSuccessAlert;

public class DebtPaymentController {

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
    public void initialize() {
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
            showFailedAlert("خطأ", "خطأ في الاتصال بقاعدة البيانات");
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
            showFailedAlert("خطأ", "خطأ في البحث.");
        }
    }

    @FXML
    private void handleCancel() {
        // Close the current window
        Stage stage = (Stage) clientIdField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleRefresh() {
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
                Cell cell = header.createCell(i);
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

            showSuccessAlert("نجاح", "تم حفظ الملف بنجاح!");

        } catch (Exception e) {
            showFailedAlert("فشل", "فشل حفظ الملف.");
        }
    }
}
