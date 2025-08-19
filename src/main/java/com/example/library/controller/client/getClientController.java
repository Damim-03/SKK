package com.example.library.controller.client;

import com.example.library.model.Client;
import com.example.library.util.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.library.Alert.alert.showAlert;
import static com.example.library.Alert.alert.showFailedAlert;

public class getClientController {

    // FXML elements
    @FXML private TextField CustomerIDField;
    @FXML private Button search;
    @FXML private TableView<Client> tableView;
    @FXML private TableColumn<Client, Boolean> selectColumn;
    @FXML private TableColumn<Client, String> customerIDColumn;
    @FXML private TableColumn<Client, String> customernameColumn;
    @FXML private TableColumn<Client, String> phoneColumn;
    @FXML private TableColumn<Client, String> placeColumn;
    @FXML private TableColumn<Client, String> TotalDebetColumn;
    @FXML private ImageView customerImage;
    @FXML private TextField CustomerID;
    @FXML private TextField CustomerName;
    @FXML private TextField PhoneNumber;
    @FXML private TextField placename;
    @FXML private TextField totalmustpay;
    @FXML private Button cleanButton;
    @FXML private Button updateListButton;

    private ObservableList<Client> clients = FXCollections.observableArrayList();

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
        customerIDColumn.setCellValueFactory(cellData -> cellData.getValue().customerIdProperty());
        customernameColumn.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        placeColumn.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        TotalDebetColumn.setCellValueFactory(cellData -> {
            double debt = cellData.getValue().getDebt();
            return new javafx.beans.property.SimpleStringProperty(debt == 0.0 ? "0.00 DZ" : String.format("%.2f DZ", debt));
        });
        tableView.setItems(clients);

        // Handle row selection
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayClientDetails(newSelection);
            } else {
                clearDetails();
            }
        });

        // Load initial client data
        loadClients();
    }

    @FXML
    private void handleSearchClient(ActionEvent event) {
        String searchId = CustomerIDField.getText().trim();
        if (searchId.isEmpty()) {
            loadClients(); // Reload all if search field is empty
            return;
        }

        clients.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showFailedAlert("خطأ", "فشل الاتصال بقاعدة البيانات.");
                return;
            }
            String sql = "SELECT customer_id, customer_name, phone, address, image_path, (SELECT COALESCE(SUM(total), 0.00) FROM client_sales WHERE client_sales.customer_id = client.customer_id) as total_debt FROM client WHERE customer_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, searchId);
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
    private void handleCleanButton(ActionEvent event) {
        clearDetails();
        CustomerIDField.clear();
        tableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleUpdateButton(ActionEvent event) {
        loadClients();
        clearDetails();
        CustomerIDField.clear();
    }

    private void loadClients() {
        clients.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showFailedAlert("خطأ", "فشل الاتصال بقاعدة البيانات.");
                return;
            }
            String sql = "SELECT customer_id, customer_name, phone, address, image_path, (SELECT COALESCE(SUM(total), 0.00) FROM client_sales WHERE client_sales.customer_id = client.customer_id) as total_debt FROM client";
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

    private void displayClientDetails(Client client) {
        CustomerID.setText(client.getCustomerId());
        CustomerName.setText(client.getCustomerName());
        PhoneNumber.setText(client.getPhone());
        placename.setText(client.getAddress());
        totalmustpay.setText(String.format("%.2f", client.getDebt()));
        String imagePath = client.getImagePath() != null ? client.getImagePath() : "/images/image.png";
        try {
            if (imagePath.startsWith("/images/customers/")) {
                // Load uploaded image from file system
                File file = new File("target/classes" + imagePath);
                customerImage.setImage(new Image(file.toURI().toString()));
            } else {
                // Load default image from resources
                customerImage.setImage(new Image(getClass().getResource(imagePath).toExternalForm()));
            }
        } catch (Exception e) {
            showFailedAlert("خطأ", "فشل تحميل صورة العميل: تأكد من وجود الملف " + imagePath);
            customerImage.setImage(null);
        }
    }

    private void clearDetails() {
        CustomerID.clear();
        CustomerName.clear();
        PhoneNumber.clear();
        placename.clear();
        totalmustpay.clear();
        customerImage.setImage(null);
    }
}