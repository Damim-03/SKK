package com.example.library.controller.client;

import com.example.library.model.ClientSale;
import com.example.library.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import static com.example.library.Alert.alert.*;

public class ClientPayDebtController {

    // Header
    @FXML private Pane headerPane;
    @FXML private Label salesScreenLabel;
    @FXML private HBox headerHBox;
    @FXML private ImageView dateIcon;
    @FXML private TextField dateField;
    @FXML private Label dateLabel;
    @FXML private ImageView timeIcon;
    @FXML private TextField timeField;
    @FXML private Label timeLabel;

    // Search
    @FXML private Pane searchPane;
    @FXML private MenuButton clientNameMenu;
    @FXML private TextField clientIdFeild;

    // Table
    @FXML private TableView<ClientSale> ClientRaportTable;
    @FXML private TableColumn<ClientSale, String> colCustomerId;
    @FXML private TableColumn<ClientSale, String> colCustomerName;
    @FXML private TableColumn<ClientSale, Number> colDiscount;
    @FXML private TableColumn<ClientSale, Number> colDebt;
    @FXML private TableColumn<ClientSale, Number> colTotal;
    @FXML private TableColumn<ClientSale, Number> colSubtotal;
    @FXML private TableColumn<ClientSale, String> colSaleDate;
    @FXML private TableColumn<ClientSale, String> colSaleTime;

    // Totals
    @FXML private Pane totalsPane;
    @FXML private Label totalLabel;

    // Arabic total
    @FXML private Pane valuesPane;
    @FXML private TextField totalField;
    @FXML private TextField ArabicTotalField;

    // Settings
    @FXML private Pane settingsPane;
    @FXML private Label discountPercentLabel;
    @FXML private Button payButton;
    @FXML private ImageView payIcon;

    // Buttons
    @FXML private Pane buttonsPane;
    @FXML private Button saveButton;
    @FXML private ImageView saveIcon;
    @FXML private Button refreshButton;
    @FXML private ImageView refreshIcon;
    @FXML private Button deleteButton;
    @FXML private ImageView deleteIcon;

    private int currentSaleId = -1; // -1 means no sale selected

    @FXML
    private void initialize() {
        clientNameMenu.setText("اختر العميل...");
        refreshButton.setOnAction(event -> handleRefresh());
        deleteButton.setOnAction(event -> handleDelete());
        saveButton.setOnAction(event -> handleExcelSave());
        payButton.setOnAction(event -> handlePayDebt());
        setupTable();
        loadClients();

        ClientRaportTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentSaleId = newSelection.getSaleId(); // Replace getSaleId() with your model’s getter
            }
        });

    }

    private void setupTable() {
        colCustomerId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCustomerId()));
        colCustomerName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCustomerName()));
        colDiscount.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDiscount()));
        colDebt.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDebt()));
        colTotal.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTotal()));
        colSubtotal.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getSubtotal()));
        colSaleDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSaleDate().toString()));
        colSaleTime.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSaleTime().toString()));
    }

    private void loadClients() {
        String query = "SELECT customer_id, customer_name FROM client";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            clientNameMenu.getItems().clear();

            while (rs.next()) {
                String id = rs.getString("customer_id");
                String name = rs.getString("customer_name");

                MenuItem item = new MenuItem(name);
                item.setOnAction(e -> {
                    clientNameMenu.setText(name);           // Show client name
                    clientNameMenu.setUserData(id);         // Store client ID for later use
                    clientIdFeild.setText(id);              // Optional: show ID in text field
                    loadClientSales(id);                    // Load sales for this client
                });

                clientNameMenu.getItems().add(item);
            }

        } catch (SQLException e) {
           showFailedAlert("فشل", "تعذر تحميل العميل.");
        }
    }

    private void loadClientSales(String customerId) {
        String query =
                "SELECT sale_id, sale_date, sale_time, subtotal, discount, debt, total, customer_name, customer_id " +
                        "FROM client_sales " +
                        "WHERE customer_id = ?";

        ObservableList<ClientSale> salesList = FXCollections.observableArrayList();
        double totalSum = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                salesList.add(new ClientSale(
                        rs.getInt("sale_id"),
                        rs.getDate("sale_date").toLocalDate(),
                        rs.getTime("sale_time").toLocalTime(),
                        rs.getBigDecimal("subtotal"),
                        rs.getBigDecimal("discount"),
                        rs.getBigDecimal("debt"),
                        rs.getBigDecimal("total"),
                        rs.getString("customer_name"),
                        rs.getString("customer_id")
                ));

                if (rs.getBigDecimal("total") != null) {
                    totalSum += rs.getBigDecimal("total").doubleValue();
                }
            }

        } catch (SQLException e) {
            showFailedAlert("فشل", "تعذر تحميل مبيعات العميل.");
        }

        // عرض البيانات في الجدول
        ClientRaportTable.setItems(salesList);
        totalField.setText(String.format("%.2f DZ", totalSum));
        totalField.setFont(Font.font("System", FontWeight.BOLD, 40));

        ArabicTotalField.setText(amountToArabicWords(totalSum));
        ArabicTotalField.setFont(Font.font("System", FontWeight.BOLD, 28));

        // ✅ التحقق من تطابق الديون بين الجدولين
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement debtStmt = conn.prepareStatement(
                     "SELECT amount FROM client_debts WHERE customer_id = ?")) {

            debtStmt.setString(1, customerId);
            ResultSet debtRs = debtStmt.executeQuery();

            if (debtRs.next()) {
                double debtAmount = debtRs.getBigDecimal("amount").doubleValue();

                if (Math.abs(debtAmount - totalSum) > 0.01) { // السماح بفارق بسيط للكسور
                    showWarningAlert("تنبيه", "⚠ الديون غير متطابقة!\n" +
                            "المديونية في client_debts: " + debtAmount +
                            "\nالمجموع من client_sales: " + totalSum);
                }
            }

        } catch (SQLException e) {
            showFailedAlert("فشل", "تحميل مبيعات الزبون.");
        }
    }


    // ======= Arabic Number to Words =======
    private static final String[] ARABIC_ONES = {
            "", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة",
            "ستة", "سبعة", "ثمانية", "تسعة", "عشرة", "أحد عشر",
            "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر",
            "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر"
    };

    private static final String[] ARABIC_TENS = {
            "", "", "عشرون", "ثلاثون", "أربعون", "خمسون",
            "ستون", "سبعون", "ثمانون", "تسعون"
    };

    private static final String[] ARABIC_HUNDREDS = {
            "", "مائة", "مائتان", "ثلاثمائة", "أربعمائة",
            "خمسمائة", "ستمائة", "سبعمائة", "ثمانمائة", "تسعمائة"
    };

    private static final String[] SCALE = {"", "ألف", "مليون", "مليار"};

    private String amountToArabicWords(double amount) {
        long integerPart = (long) amount;
        int fractionPart = (int) Math.round((amount - integerPart) * 100);

        String result = integerToArabicWords(integerPart) + " دينار جزائري";
        if (fractionPart > 0) {
            result += " و " + integerToArabicWords(fractionPart) + " سنتيم";
        }
        return result;
    }

    private String integerToArabicWords(long number) {
        if (number == 0) return "صفر";

        StringBuilder words = new StringBuilder();
        int scaleIndex = 0;

        while (number > 0) {
            int group = (int) (number % 1000);
            if (group != 0) {
                String groupWords = threeDigitsToWords(group);
                if (!groupWords.isEmpty()) {
                    if (scaleIndex > 0) {
                        if (group == 2) groupWords = "ألفان";
                        else groupWords += " " + SCALE[scaleIndex];
                    }
                    if (words.length() > 0) words.insert(0, " و ");
                    words.insert(0, groupWords);
                }
            }
            number /= 1000;
            scaleIndex++;
        }
        return words.toString();
    }

    private String threeDigitsToWords(int number) {
        StringBuilder result = new StringBuilder();

        int hundreds = number / 100;
        int tensOnes = number % 100;
        int tens = tensOnes / 10;
        int ones = tensOnes % 10;

        if (hundreds > 0) {
            result.append(ARABIC_HUNDREDS[hundreds]);
            if (tensOnes > 0) result.append(" و ");
        }

        if (tensOnes < 20) {
            result.append(ARABIC_ONES[tensOnes]);
        } else {
            if (ones > 0) {
                result.append(ARABIC_ONES[ones]).append(" و ");
            }
            result.append(ARABIC_TENS[tens]);
        }

        return result.toString().trim();
    }


    private void handleRefresh() {
        String currentCustomerId = clientIdFeild.getText();

        if (currentCustomerId != null && !currentCustomerId.isEmpty()) {
            loadClientSales(currentCustomerId); // reload sales for selected client
        }

        loadClients(); // reload client list
    }


    private void handleDelete() {
        if (currentSaleId == -1) {
            showFailedAlert("خطأ", "لم يتم تحديد أي فاتورة للحذف.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setHeaderText(null);
        confirm.setContentText("هل أنت متأكد أنك تريد حذف هذه الفاتورة؟");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false); // بدء المعاملة

                String customerId = null;
                BigDecimal debtAmount = BigDecimal.ZERO;

                // ✅ 1. جلب قيمة الدين والعميل
                String debtQuery = "SELECT total, customer_id FROM client_sales WHERE sale_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(debtQuery)) {
                    stmt.setInt(1, currentSaleId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        debtAmount = rs.getBigDecimal("total");
                        customerId = rs.getString("customer_id");
                    } else {
                        showFailedAlert("خطأ", "الفاتورة غير موجودة.");
                        return;
                    }
                }

                // ✅ 2. حذف التفاصيل من client_sales_item
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM client_sales_item WHERE sale_id = ?")) {
                    stmt.setInt(1, currentSaleId);
                    stmt.executeUpdate();
                }

                // ✅ 3. حذف الفاتورة من client_sales
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM client_sales WHERE sale_id = ?")) {
                    stmt.setInt(1, currentSaleId);
                    stmt.executeUpdate();
                }

                // ✅ 4. تحديث الديون إذا كان هناك دين فعلاً
                if (customerId != null && debtAmount != null && debtAmount.compareTo(BigDecimal.ZERO) > 0) {
                    String updateDebt = "UPDATE client_debts SET amount = amount - ? WHERE customer_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(updateDebt)) {
                        stmt.setBigDecimal(1, debtAmount);
                        stmt.setString(2, customerId);
                        stmt.executeUpdate();
                    }
                }

                conn.commit(); // تأكيد التعديلات
                showSuccessAlert("تم الحذف", "تم حذف الفاتورة وتحديث الديون بنجاح.");

                // ✅ تحديث الواجهة
                loadClientSales(customerId);
                loadClients();

            } catch (SQLException e) {
                e.printStackTrace();
                showFailedAlert("خطأ", "فشل في حذف الفاتورة.");
            }
        }
    }

    @FXML
    private void handleExcelSave() {
        String clientName = clientNameMenu.getText();
        if (clientName == null || clientName.trim().isEmpty() || clientName.equals("اختر العميل...")) {
            showAlert("تنبيه", "يرجى اختيار العميل أولاً!");
            return;
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Client Data");

        // ====== Create Header Row ======
        Row headerRow = sheet.createRow(0);
        for (int col = 0; col < ClientRaportTable.getColumns().size(); col++) {
            TableColumn<?, ?> column = ClientRaportTable.getColumns().get(col);
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(column.getText());
        }

        // ====== Fill Data ======
        for (int rowIdx = 0; rowIdx < ClientRaportTable.getItems().size(); rowIdx++) {
            Row excelRow = sheet.createRow(rowIdx + 1);

            for (int col = 0; col < ClientRaportTable.getColumns().size(); col++) {
                TableColumn<?, ?> column = ClientRaportTable.getColumns().get(col);
                Object cellValue = column.getCellData(rowIdx);
                Cell cell = excelRow.createCell(col);
                cell.setCellValue(cellValue == null ? "" : cellValue.toString());
            }
        }

        // Auto-size columns
        for (int col = 0; col < ClientRaportTable.getColumns().size(); col++) {
            sheet.autoSizeColumn(col);
        }

        // ====== Save File ======
        String safeName = clientName.replaceAll("[\\\\/:*?\"<>|]", "_");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("حفظ الملف");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName(safeName + ".xlsx");

        File file = fileChooser.showSaveDialog(ClientRaportTable.getScene().getWindow());
        if (file == null) return;

        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
            workbook.close();
            showAlert("تم الحفظ", "تم حفظ الملف باسم: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("خطأ", "فشل حفظ الملف: " + e.getMessage());
        }
    }

    @FXML
    private void handlePayDebt() {
        String customerId = clientIdFeild.getText().trim();
        String customerName = clientNameMenu.getText().trim(); // Assuming clientNameMenu is defined

        if (customerId.isEmpty() || customerName.isEmpty() || customerName.equals("اختر العميل...")) {
            showAlert("تنبيه", "الرجاء اختيار العميل أولاً.");
            return;
        }

        // Fetch current debt from client_debts
        BigDecimal currentDebt;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String fetchDebtSQL = "SELECT amount FROM client_debts WHERE customer_id = ? AND status IN ('غير مدفوع', 'مدفوع جزئيًا')";
            try (PreparedStatement ps = conn.prepareStatement(fetchDebtSQL)) {
                ps.setString(1, customerId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentDebt = rs.getBigDecimal("amount");
                } else {
                    showAlert("تنبيه", "لا يوجد دين مستحق لهذا العميل.");
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("خطأ", "فشل في جلب بيانات الدين: " + e.getMessage());
            return;
        }

        // Automatically set amountPaid to the full currentDebt
        BigDecimal amountPaid = currentDebt;
        BigDecimal remainingDebt = BigDecimal.ZERO; // Since we’re paying the full amount
        boolean isFullyPaid = true;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Record the payment with status "تم الدفع"
            String insertPaymentSQL = "INSERT INTO client_payments (customer_id, customer_name, amount_paid, payment_date, payment_time, status) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertPaymentSQL)) {
                ps.setString(1, customerId);
                ps.setString(2, customerName);
                ps.setBigDecimal(3, amountPaid);
                ps.setDate(4, java.sql.Date.valueOf(LocalDateTime.now().toLocalDate())); // 2025-08-15
                ps.setTime(5, java.sql.Time.valueOf(LocalDateTime.now().toLocalTime().withNano(0))); // 00:03 AM CET
                ps.setString(6, "تم الدفع"); // Set status to "Payment Completed"
                ps.executeUpdate();
            }

            // Delete debt from client_debts since it’s fully paid
            String deleteDebtSQL = "DELETE FROM client_debts WHERE customer_id = ? AND amount = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteDebtSQL)) {
                ps.setString(1, customerId);
                ps.setBigDecimal(2, currentDebt);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    showAlert("خطأ", "فشل في حذف الدين. قد تكون البيانات تم تعديلها.");
                    conn.rollback();
                    return;
                }
            }

            // Delete sales and items since debt is fully paid
            String deleteItemsSQL = "DELETE FROM client_sales_item WHERE sale_id IN (SELECT sale_id FROM client_sales WHERE customer_id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(deleteItemsSQL)) {
                ps.setString(1, customerId);
                ps.executeUpdate();
            }

            String deleteSalesSQL = "DELETE FROM client_sales WHERE customer_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSalesSQL)) {
                ps.setString(1, customerId);
                ps.executeUpdate();
            }

            conn.commit();
            String message = "تم دفع الدين بنجاح. تم حذف البيانات.";
            showAlert("تم", message);
            handleExcelSave();
            handleRefresh(); // Assuming handleRefresh is defined

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("خطأ", "فشل تسجيل الدفع: " + e.getMessage());
        }
    }
}
