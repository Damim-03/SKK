package com.example.library.controller.client;

import com.example.library.model.Client;
import com.example.library.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static com.example.library.Alert.alert.showFailedAlert;

public class settingsClientController {

    // FXML elements
    @FXML private TextField CustomerNameField;
    @FXML private Label CustomerName;
    @FXML private TextField CustomerIDField;
    @FXML private Label CustomerID;
    @FXML private TableView<Client> tableView;
    @FXML private TableColumn<Client, Boolean> selectColumn;
    @FXML private TableColumn<Client, String> barcodeColumn;
    @FXML private TableColumn<Client, String> nameColumn;
    @FXML private TableColumn<Client, String> phoneColumn;
    @FXML private TableColumn<Client, String> placeColumn;
    @FXML private TableColumn<Client, String> TotaldebtColumn;
    @FXML private Button exitButton;
    @FXML private Button updateListButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;

    private ObservableList<Client> clients = FXCollections.observableArrayList();
    private PauseTransition idSearchDebounce = new PauseTransition(Duration.millis(300));
    private PauseTransition nameSearchDebounce = new PauseTransition(Duration.millis(300));

    @FXML
    public void initialize() {
        // Configure TableView columns
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= tableView.getItems().size()) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item != null && item);
                    checkBox.setOnAction(e -> tableView.getItems().get(getIndex()).setSelected(checkBox.isSelected()));
                    setGraphic(checkBox);
                }
            }
        });
        barcodeColumn.setCellValueFactory(cellData -> cellData.getValue().customerIdProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        placeColumn.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        TotaldebtColumn.setCellValueFactory(cellData -> {
            double debt = cellData.getValue().getDebt();
            return new javafx.beans.property.SimpleStringProperty(debt == 0.0 ? "0.00 DZ" : String.format("%.2f DZ", debt));
        });
        tableView.setItems(clients);

        // Add real-time search listeners
        CustomerIDField.textProperty().addListener((obs, oldValue, newValue) -> {
            idSearchDebounce.setOnFinished(e -> searchByCustomerID(newValue.trim()));
            idSearchDebounce.playFromStart();
        });

        CustomerNameField.textProperty().addListener((obs, oldValue, newValue) -> {
            nameSearchDebounce.setOnFinished(e -> searchByCustomerName(newValue.trim()));
            nameSearchDebounce.playFromStart();
        });

        // Add TextFormatter for numeric input only
        CustomerIDField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty() || newText.matches("\\d*")) { // Allow empty or numeric only
                return change;
            }
            return null; // Reject non-numeric input
        }));

        // Load initial client data
        loadClients();
    }

    private void searchByCustomerID(String searchId) {
        if (searchId.isEmpty()) {
            loadClients();
            return;
        }

        clients.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showFailedAlert("خطأ", "فشل الاتصال بقاعدة البيانات.");
                return;
            }
            String sql = "SELECT customer_id, customer_name, phone, address, image_path, (SELECT COALESCE(SUM(total), 0.0) FROM client_sales WHERE client_sales.customer_id = client.customer_id) as total_debt FROM client WHERE customer_id LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, searchId + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Client client = new Client(
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("image_path"),
                        rs.getDouble("total_debt")
                );
                clients.add(client);
                tableView.getSelectionModel().select(client);
            } else {
                showFailedAlert("خطأ", "لم يتم العثور على عميل برقم المعرف: " + searchId);
            }
        } catch (SQLException e) {
            showFailedAlert("خطأ", "فشل البحث عن العميل: " + e.getMessage());
        }
    }

    @FXML
    private void handleCustomerIDSearch(ActionEvent event) {
        searchByCustomerID(CustomerIDField.getText().trim());
    }

    private void searchByCustomerName(String searchName) {
        if (searchName.isEmpty()) {
            loadClients();
            return;
        }

        clients.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showFailedAlert("خطأ", "فشل الاتصال بقاعدة البيانات.");
                return;
            }
            String sql = "SELECT customer_id, customer_name, phone, address, image_path, (SELECT COALESCE(SUM(total), 0.0) FROM client_sales WHERE client_sales.customer_id = client.customer_id) as total_debt FROM client WHERE customer_name LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + searchName + "%");
            ResultSet rs = stmt.executeQuery();
            boolean found = false;
            while (rs.next()) {
                Client client = new Client(
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("image_path"),
                        rs.getDouble("total_debt")
                );
                clients.add(client);
                found = true;
            }
            if (!found) {
                showFailedAlert("خطأ", "لم يتم العثور على عميل مطابق لـ: " + searchName);
            } else {
                tableView.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            showFailedAlert("خطأ", "فشل البحث عن العميل: " + e.getMessage());
        }
    }

    @FXML
    private void handleCustomerNameSearch(ActionEvent event) {
        searchByCustomerName(CustomerNameField.getText().trim());
    }

    @FXML
    private void handleExitButton(ActionEvent event) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleUpdateButton(ActionEvent event) {
        loadClients();
        CustomerIDField.clear();
        CustomerNameField.clear();
    }

    @FXML
    private void handleUpdateClientButton(ActionEvent event) throws IOException {
        Client selectedClient = tableView.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showFailedAlert("خطأ", "يرجى تحديد عميل للتحديث.");
            return;
        }
        String fxmlPath = "/com/example/interfaces/client/CRUD/update_client.fxml";
        if (getClass().getResource(fxmlPath) == null) {
            showFailedAlert("خطأ", "لم يتم العثور على الملف: " + fxmlPath);
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        updateClientController controller = loader.getController();
        controller.setClient(selectedClient);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("تحديث العميل");
        stage.showAndWait();
        loadClients();
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        Client selectedClient = tableView.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showFailedAlert("خطأ", "يرجى تحديد عميل للحذف.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showFailedAlert("خطأ", "فشل الاتصال بقاعدة البيانات.");
                return;
            }
            String deleteSalesSql = "DELETE FROM client_sales WHERE customer_id = ?";
            PreparedStatement deleteSalesStmt = conn.prepareStatement(deleteSalesSql);
            deleteSalesStmt.setString(1, selectedClient.getCustomerId());
            deleteSalesStmt.executeUpdate();

            String deleteClientSql = "DELETE FROM client WHERE customer_id = ?";
            PreparedStatement deleteClientStmt = conn.prepareStatement(deleteClientSql);
            deleteClientStmt.setString(1, selectedClient.getCustomerId());
            int rowsAffected = deleteClientStmt.executeUpdate();

            if (rowsAffected > 0) {
                clients.remove(selectedClient);
                CustomerIDField.clear();
                CustomerNameField.clear();
                showFailedAlert("نجاح", "تم حذف العميل بنجاح: " + selectedClient.getCustomerName());
            } else {
                showFailedAlert("خطأ", "فشل حذف العميل.");
            }
        } catch (SQLException e) {
            showFailedAlert("خطأ", "فشل حذف العميل: " + e.getMessage());
        }
    }

    private void loadClients() {
        clients.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showFailedAlert("خطأ", "فشل الاتصال بقاعدة البيانات.");
                return;
            }
            String sql = "SELECT customer_id, customer_name, phone, address, image_path, (SELECT COALESCE(SUM(total), 0.0) FROM client_sales WHERE client_sales.customer_id = client.customer_id) as total_debt FROM client";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Client client = new Client(
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("image_path"),
                        rs.getDouble("total_debt")
                );
                clients.add(client);
            }
        } catch (SQLException e) {
            showFailedAlert("خطأ", "فشل تحميل العملاء: " + e.getMessage());
        }
    }
}