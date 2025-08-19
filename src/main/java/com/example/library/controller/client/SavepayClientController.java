package com.example.library.controller.client;

import com.example.library.model.Debt;
import com.example.library.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;
import java.time.LocalDate;

import static com.example.library.Alert.alert.showFailedAlert;

public class SavepayClientController {

    @FXML private TableView<Debt> debtTableView;
    @FXML private TableColumn<Debt, String> colCustomerId;
    @FXML private TableColumn<Debt, String> colCustomerName;
    @FXML private TableColumn<Debt, BigDecimal> colTotalDebt;
    @FXML private TableColumn<Debt, LocalDate> colDebtDate;
    @FXML private TableColumn<Debt, String> colNotes;

    private ObservableList<Debt> debtList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // ربط الأعمدة بالخصائص
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colTotalDebt.setCellValueFactory(new PropertyValueFactory<>("totalDebt"));
        colDebtDate.setCellValueFactory(new PropertyValueFactory<>("debtDate"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        loadDebts();
    }

    private void loadDebts() {
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
                LocalDate lastDebtDate = rs.getDate("debtDate").toLocalDate();
                String notes = rs.getString("all_notes");

                debtList.add(new Debt(customerId, customerName, totalDebt, lastDebtDate, notes));
            }

            debtTableView.setItems(debtList);

        } catch (Exception e) {
            showFailedAlert("خطأ", "لم يتم حفظ الدفع.");
        }
    }
}
