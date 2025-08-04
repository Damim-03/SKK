package com.example.library.controller.inventory;

import com.example.library.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.UUID;

public class importDataController {

    @FXML
    private TextField pathField;

    @FXML
    private Button browseButton, checkButton, importButton;

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
    private void handleCheckData() {
        if (!validateFile()) return;
        readExcelFile(selectedFile, false);
    }

    @FXML
    private void handleImportData() {
        if (!validateFile()) return;
        readExcelFile(selectedFile, true);
    }

    private boolean validateFile() {
        String path = pathField.getText();
        if (path == null || path.trim().isEmpty()) {
            showAlert("خطأ", "يرجى اختيار ملف Excel أولاً.");
            return false;
        }

        selectedFile = new File(path);
        if (!selectedFile.exists() || !selectedFile.getName().endsWith(".xlsx")) {
            showAlert("خطأ", "الملف غير صالح أو ليس بصيغة xlsx.");
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
                    System.out.println("✔ التحقق: " + id + " | " + barcode + " | " + name + " | " +
                            description + " | " + price1 + " | " + price2 + " | " +
                            price3 + " | " + unit + " | " + quantity + " | " +
                            productionDate + " | " + expirationDate + " | " +
                            imagePath + " | " + category);
                }
            }

            String alertMessage = saveToDatabase
                    ? "تم استيراد " + successCount + " منتج(ات) بنجاح.\n" +
                    (skippedCount > 0 ? skippedCount + " منتج(ات) تم تخطيها." : "")
                    : "تم التحقق من صحة البيانات.";

            if (skippedCount > 0) {
                alertMessage += "\n\nالمنتجات المتخطاة:\n" + skippedProducts.toString();
            }

            showAlert("نتيجة", alertMessage);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("خطأ", "حدث خطأ أثناء قراءة الملف: " + e.getMessage());
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
            e.printStackTrace();
        }
        return false;
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean insertProduct(String id, String barcode, String name, String description,
                                  double price1, double price2, double price3, String unit, int quantity,
                                  String productionDate, String expirationDate, String imagePath, String category) {

        String query = "INSERT INTO products (" +
                "id, barcode, product_name, description, " +
                "price1, price2, price3, unit, quantity, " +
                "production_date, expiration_date, image_path, category, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, id.isEmpty() ? UUID.randomUUID().toString() : id);
            stmt.setString(2, barcode);
            stmt.setString(3, name);
            stmt.setString(4, description);
            stmt.setDouble(5, price1);
            stmt.setDouble(6, price2);
            stmt.setDouble(7, price3);
            stmt.setString(8, unit.isEmpty() ? "N/A" : unit);
            stmt.setInt(9, quantity);

            if (productionDate != null && !productionDate.trim().isEmpty()) {
                stmt.setDate(10, Date.valueOf(productionDate));
            } else {
                stmt.setNull(10, Types.DATE);
            }

            if (expirationDate != null && !expirationDate.trim().isEmpty()) {
                stmt.setDate(11, Date.valueOf(expirationDate));
            } else {
                stmt.setNull(11, Types.DATE);
            }

            stmt.setString(12, (imagePath == null || imagePath.trim().isEmpty()) ? null : imagePath);
            stmt.setString(13, category);

            stmt.executeUpdate();
            return true;

        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("⚠ خطأ أثناء إدخال المنتج: " + name + " - " + e.getMessage());
            return false;
        }
    }
}
