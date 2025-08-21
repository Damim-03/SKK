package com.example.library.controller.client;

import com.example.library.model.Debt;
import com.example.library.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;
import java.time.LocalDate;

import static com.example.library.Alert.alert.showFailedAlert;

public class SavepayClientController {

    @FXML
    private TableView<Debt> debtTableView;

    @FXML
    private TableColumn<Debt, String> colCustomerId;

    @FXML
    private TableColumn<Debt, String> colCustomerName;

    @FXML
    private TableColumn<Debt, BigDecimal> colTotalDebt;

    @FXML
    private TableColumn<Debt, LocalDate> colDebtDate;

    @FXML
    private TableColumn<Debt, String> colNotes;

    @FXML
    private TextField searchCustomerIdField;

    @FXML
    private TextField searchCustomerNameField;

    private ObservableList<Debt> debtList = FXCollections.observableArrayList();
    private FilteredList<Debt> filteredDebts;

    @FXML
    public void initialize() {
        // ربط الأعمدة بالخصائص
        setupTableColumns();
        setupSearchListeners();
        loadAllDebts();
    }

    private void setupTableColumns() {
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colTotalDebt.setCellValueFactory(new PropertyValueFactory<>("totalDebt"));
        colDebtDate.setCellValueFactory(new PropertyValueFactory<>("debtDate"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
    }

    private void setupSearchListeners() {
        // إضافة مستمعين للحقول للبحث الفوري
        searchCustomerIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilter();
        });

        searchCustomerNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilter();
        });
    }

    // تحميل كل الديون
    private void loadAllDebts() {
        debtList.clear();
        String sql =
                "SELECT c.customer_id, c.customer_name, " +
                        "       SUM(cd.amount) AS total_debt, " +
                        "       MAX(cd.debt_date) AS debtDate, " +
                        "       GROUP_CONCAT(cd.notes SEPARATOR ', ') AS all_notes " +
                        "FROM client_debts cd " +
                        "JOIN client c ON cd.customer_id = c.customer_id " +
                        "GROUP BY c.customer_id, c.customer_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String customerId = rs.getString("customer_id");
                String customerName = rs.getString("customer_name");
                BigDecimal totalDebt = rs.getBigDecimal("total_debt");

                // Handle possible null date
                LocalDate lastDebtDate = null;
                if (rs.getDate("debtDate") != null) {
                    lastDebtDate = rs.getDate("debtDate").toLocalDate();
                }

                String notes = rs.getString("all_notes");

                debtList.add(new Debt(customerId, customerName, totalDebt, lastDebtDate, notes));
            }

            // إعداد البحث المصفى للبحث الفوري
            filteredDebts = new FilteredList<>(debtList, p -> true);

            // ربط الجدول بالقائمة المصفاة
            SortedList<Debt> sortedData = new SortedList<>(filteredDebts);
            sortedData.comparatorProperty().bind(debtTableView.comparatorProperty());
            debtTableView.setItems(sortedData);

        } catch (Exception e) {
            showFailedAlert("خطأ", "فشل في تحميل بيانات الديون.");
        }
    }

    // تطبيق الفلتر للبحث الفوري
    private void applyFilter() {
        String customerId = searchCustomerIdField.getText().trim().toLowerCase();
        String customerName = searchCustomerNameField.getText().trim().toLowerCase();

        filteredDebts.setPredicate(debt -> {
            // إذا كانت حقول البحث فارغة، اعرض كل البيانات
            if (customerId.isEmpty() && customerName.isEmpty()) {
                return true;
            }

            boolean matchesCustomerId = customerId.isEmpty() ||
                    debt.getCustomerId().toLowerCase().contains(customerId);

            boolean matchesCustomerName = customerName.isEmpty() ||
                    debt.getCustomerName().toLowerCase().contains(customerName);

            return matchesCustomerId && matchesCustomerName;
        });
    }
}