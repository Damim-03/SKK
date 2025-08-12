package com.example.library.controller.inventory;

import com.example.library.util.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.collections.ObservableList;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

import static com.example.library.Alert.alert.*;

public class importDataController {

    @FXML
    private TextField pathField;

    private Stage previewStage = null;

    @FXML
    private Button browseButton, checkButton, importButton;

    // متغير لتتبع إذا تم التحقق
    private boolean isDataChecked = false;

    private File selectedFile;

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("اختر ملف Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        Stage stage = (Stage) pathField.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            pathField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleCheckData(javafx.event.ActionEvent event) {
        boolean checkPassed = validateFile();

        if (checkPassed) {
            isDataChecked = true;
            importButton.setDisable(false); // تفعيل زر الاستيراد
            readExcelFile(selectedFile, false);
        } else {
            isDataChecked = false;
            importButton.setDisable(true); // إيقاف زر الاستيراد
            showFailedAlert("خطأ", "فشل التحقق من البيانات. الرجاء التصحيح قبل الاستيراد.");
        }
    }


    @FXML
    private void handleImportData(javafx.event.ActionEvent event) {
        // إذا لم يتم التحقق، أوقف العملية وأظهر رسالة
        if (!isDataChecked) {
            return;
        }

        // منطق الاستيراد
        if (!validateFile()) return;
        readExcelFile(selectedFile, true);
    }


    private boolean validateFile() {
        String path = pathField.getText();
        if (path == null || path.trim().isEmpty()) {
            showFailedAlert("خطأ", "يرجى اختيار ملف Excel أولاً.");
            return false;
        }

        selectedFile = new File(path);
        if (!selectedFile.exists() || !selectedFile.getName().endsWith(".xlsx")) {
            showFailedAlert("خطأ", "الملف غير صالح أو ليس بصيغة xlsx.");
            return false;
        }

        return true;
    }

    private void readExcelFile(File file, boolean saveToDatabase) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            if (rows.hasNext()) rows.next(); // Skip header

            int successCount = 0;
            int skippedCount = 0;
            StringBuilder skippedProducts = new StringBuilder();

            while (rows.hasNext()) {
                Row row = rows.next();

                String id = getStringCell(row, 0);
                String barcode = getBarcodeCell(row, 1);
                String name = getStringCell(row, 2);
                String description = getStringCell(row, 3);
                double price1 = getNumericCell(row, 4);
                double price2 = getNumericCell(row, 5);
                double price3 = getNumericCell(row, 6);
                String unit = getStringCell(row, 7);
                int quantity = (int) getNumericCell(row, 8);
                String productionDate = getDateCell(row, 9);
                String expirationDate = getDateCell(row, 10);
                String imagePath = getStringCell(row, 11);
                String rawCategory = getStringCell(row, 12);
                String category = cleanCategory(rawCategory);

                if (saveToDatabase) {
                    if (barcodeExists(barcode)) {
                        skippedCount++;
                        skippedProducts.append("⚠ الباركود موجود مسبقاً: ").append(barcode)
                                .append(" - المنتج: ").append(name).append("\n");
                        continue;
                    }

                    if (insertProduct(id, barcode, name, description, price1, price2, price3,
                            unit, quantity, productionDate, expirationDate, imagePath, category)) {
                        successCount++;
                    } else {
                        skippedCount++;
                        skippedProducts.append("⚠ فشل إدخال المنتج: ").append(name)
                                .append(" (باركود: ").append(barcode).append(")\n");
                    }
                } else {
                    ObservableList<ObservableList<String>> excelData = FXCollections.observableArrayList();
                    int columnCount = 0;

                    for (Row sheetRow : sheet) {
                        ObservableList<String> rowData = FXCollections.observableArrayList();
                        for (Cell cell : sheetRow) {
                            rowData.add(getStringCell(cell)); // ترجع String
                        }
                        excelData.add(rowData);
                        if (sheetRow.getLastCellNum() > columnCount) {
                            columnCount = sheetRow.getLastCellNum();
                        }
                    }

                    showExcelPreview(excelData, columnCount);
                }

            }

            String alertMessage = saveToDatabase
                    ? "تم استيراد " + successCount + " منتج(ات) بنجاح.\n" +
                    (skippedCount > 0 ? skippedCount + " منتج(ات) تم تخطيها." : "")
                    : "تم التحقق من صحة البيانات.";

            if (skippedCount > 0) {
                alertMessage += "\n\nالمنتجات المتخطاة:\n" + skippedProducts.toString();
            }

            showSuccessAlert("نتيجة", alertMessage);

        } catch (Exception e) {
            showAlert("خطأ", "حدث خطأ أثناء قراءة الملف: " + e.getMessage());
        }
    }


    private void showExcelPreview(ObservableList<ObservableList<String>> data, int columnCount) {
        if (previewStage == null) {
            previewStage = new Stage();
            previewStage.setTitle("معاينة بيانات Excel");

            TableView<ObservableList<String>> tableView = new TableView<>();

            // إنشاء الأعمدة مع CellFactory لتلوين الخلايا الفارغة بالبرتقالي
            for (int i = 0; i < columnCount; i++) {
                final int colIndex = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>("عمود " + (i + 1));
                col.setCellValueFactory(param ->
                        new SimpleStringProperty(
                                param.getValue().size() > colIndex ? param.getValue().get(colIndex) : ""
                        )
                );

                col.setCellFactory(tc -> new TableCell<>() {
                    @Override
                    protected void updateItem(String value, boolean empty) {
                        super.updateItem(value, empty);
                        if (empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(value);
                            if (value == null || value.trim().isEmpty()) {
                                setStyle("-fx-background-color: #FFD580;"); // برتقالي فاتح
                            } else {
                                setStyle("");
                            }
                        }
                    }
                });

                col.setPrefWidth(120);
                col.setMinWidth(80);
                tableView.getColumns().add(col);
            }

            // تلوين الصفوف حسب الحالة
            tableView.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(ObservableList<String> row, boolean empty) {
                    super.updateItem(row, empty);
                    if (empty || row == null) {
                        setStyle("");
                        return;
                    }

                    boolean allEmpty = row.stream().allMatch(cell -> cell == null || cell.trim().isEmpty());
                    boolean hasEmptyCell = row.stream().anyMatch(cell -> cell == null || cell.trim().isEmpty());

                    if (allEmpty) {
                        setStyle(""); // اللون الافتراضي
                    } else if (hasEmptyCell) {
                        setStyle("-fx-background-color: #FFCCCC;"); // أحمر فاتح
                    } else {
                        setStyle("-fx-background-color: #CCFFCC;"); // أخضر فاتح
                    }
                }
            });

            VBox root = new VBox(tableView);
            VBox.setVgrow(tableView, Priority.ALWAYS);
            Scene scene = new Scene(root, 900, 600);
            previewStage.setScene(scene);
            previewStage.setResizable(true);
            previewStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/excel.png"))));
        }

        // Update the table data in the existing stage
        TableView<ObservableList<String>> tableView = (TableView<ObservableList<String>>) previewStage.getScene().getRoot().getChildrenUnmodifiable().get(0);
        tableView.setItems(data);

        if (!previewStage.isShowing()) {
            previewStage.show();
        } else {
            previewStage.toFront();
        }
    }

    private boolean barcodeExists(String barcode) {
        String query = "SELECT COUNT(*) FROM products WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            showFailedAlert("خطأ", "Barcode غير موجود");
        }
        return false;
    }

    private String getStringCell(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                }
                double num = cell.getNumericCellValue();
                // Remove decimal if the number is a whole number
                if (num == (long) num) {
                    return String.format("%d", (long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    num = cell.getNumericCellValue();
                    return num == (long) num ? String.format("%d", (long) num) : String.valueOf(num);
                }
            case BLANK:
            default:
                return "";
        }
    }

    private String getBarcodeCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell != null) {
            if (cell.getCellType() == CellType.NUMERIC) {
                java.text.DecimalFormat df = new java.text.DecimalFormat("#");
                return df.format(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue().trim();
            }
        }
        return "";
    }

    private String getStringCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                }
                double num = cell.getNumericCellValue();
                return num == (long) num ? String.format("%d", (long) num) : String.valueOf(num);
            case STRING:
                return cell.getStringCellValue().trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private double getNumericCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private String getDateCell(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
        }

        if (cell.getCellType() == CellType.STRING) {
            String text = cell.getStringCellValue().trim();
            if (!text.isEmpty()) {
                try {
                    // Try to parse it
                    java.util.Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(text);
                    return new SimpleDateFormat("yyyy-MM-dd").format(parsedDate);
                } catch (Exception e) {
                    return null; // parsing failed
                }
            }
        }

        return null;
    }

    private String cleanCategory(String rawCategory) {
        if (rawCategory == null || rawCategory.trim().isEmpty()) return "غير مصنف";
        String cleaned = rawCategory.replaceAll("[^\\p{L}\\p{N}\\s]", "").trim();
        return cleaned.isEmpty() ? "غير مصنف" : cleaned;
    }

    private boolean insertProduct(String id, String barcode, String name, String description,
                                  double price1, double price2, double price3, String unit, int quantity,
                                  String productionDate, String expirationDate, String imagePath, String category) {

        // Generate UUID if id is null, empty, or invalid
        if (id == null || id.trim().isEmpty() || !isValidUUID(id)) {
            id = UUID.randomUUID().toString();
        }

        String query = "INSERT INTO products (" +
                "id, barcode, product_name, description, " +
                "price1, price2, price3, unit, quantity, " +
                "production_date, expiration_date, image_path, category, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, id);
            stmt.setString(2, barcode);
            stmt.setString(3, name);
            stmt.setString(4, description);
            stmt.setDouble(5, price1);
            stmt.setDouble(6, price2);
            stmt.setDouble(7, price3);
            stmt.setString(8, unit != null && !unit.trim().isEmpty() ? unit : "N/A");
            stmt.setInt(9, quantity);

            // Handle production date
            if (productionDate != null && !productionDate.trim().isEmpty()) {
                try {
                    stmt.setDate(10, Date.valueOf(productionDate));
                } catch (IllegalArgumentException e) {
                    stmt.setNull(10, Types.DATE);
                    showWarningAlert("تحذير", "خطأ في صيغة التاريخ الانتاج."
                            + name + ": " + productionDate
                    );

                }
            } else {
                stmt.setNull(10, Types.DATE);
            }

            // Handle expiration date
            if (expirationDate != null && !expirationDate.trim().isEmpty()) {
                try {
                    stmt.setDate(11, Date.valueOf(expirationDate));
                } catch (IllegalArgumentException e) {
                    stmt.setNull(11, Types.DATE);
                    showWarningAlert("تحذير",
                            "خطأ في صيغة التاريخ انتهاء الصلاحية."
                                    + name + ": " + expirationDate
                    );
                }
            } else {
                stmt.setNull(11, Types.DATE);
            }

            stmt.setString(12, imagePath != null && !imagePath.trim().isEmpty() ? imagePath : null);
            stmt.setString(13, category != null && !category.trim().isEmpty() ? category : "غير مصنف");

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            showWarningAlert("تحذير",
                    "خطأ أثناء إدخال المنتج:"
                            + name + " - " + e.getMessage()
            );
            return false;
        }
    }

    // Helper method to validate UUID
    private boolean isValidUUID(String uuid) {
        if (uuid == null) return false;
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}