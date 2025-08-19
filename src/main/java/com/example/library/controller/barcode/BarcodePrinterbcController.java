package com.example.library.controller.barcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import static com.example.library.Alert.alert.showWarningAlert;

public class BarcodePrinterbcController {

    @FXML private TextField txtLabelsPerRow;
    @FXML private TextField txtProductName;
    @FXML private TextField txtLabelHeight; // الطول
    @FXML private TextField txtLabelHeight1; // العرض
    @FXML private TextField txtFontSize; // السعر
    @FXML private TextField txtMarginTop;
    @FXML private TextField txtMarginLeft;
    @FXML private TextField txtMarginRight;
    @FXML private TextField txtMarginBottom;
    @FXML private Button btnPreview;
    @FXML private Button btnCancel;

    @FXML
    private void handlePreview() {
        try {
            int labelsPerRow = Integer.parseInt(txtLabelsPerRow.getText());
            String productName = txtProductName.getText();
            double labelHeight = Double.parseDouble(txtLabelHeight.getText());
            double labelWidth = Double.parseDouble(txtLabelHeight1.getText());
            double price = Double.parseDouble(txtFontSize.getText());
            int marginTop = Integer.parseInt(txtMarginTop.getText());
            int marginLeft = Integer.parseInt(txtMarginLeft.getText());
            int marginRight = Integer.parseInt(txtMarginRight.getText());
            int marginBottom = Integer.parseInt(txtMarginBottom.getText());

            // توليد رقم EAN-13 للمنتج
            String productCode = generateEAN13(productName.hashCode());
            if (productCode.length() != 13) {
                showWarningAlert("خطأ", "رقم المنتج يجب أن يكون مكون من 13 رقماً.");
                return;
            }

            int totalLabels = 10; // عدد اللاصقات للمعاينة

            GridPane grid = new GridPane();
            grid.setHgap(marginLeft);
            grid.setVgap(marginTop);
            grid.setStyle("-fx-padding: " + marginTop + " " + marginRight + " " + marginBottom + " " + marginLeft);

            for (int i = 0; i < totalLabels; i++) {
                ImageView barcodeImage = generateBarcodeImage(productCode, (int) labelWidth, (int) labelHeight);

                VBox labelBox = new VBox(5);
                labelBox.setPrefHeight(labelHeight);
                labelBox.setPrefWidth(labelWidth);
                labelBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 0.5; -fx-alignment: center;");

                Text nameText = new Text(productName);
                nameText.setFont(new Font(12));

                Text codeText = new Text(productCode);
                codeText.setFont(new Font(10));

                Text priceText = new Text(price + " DZ");
                priceText.setFont(new Font(10));

                labelBox.getChildren().addAll(nameText, barcodeImage, codeText, priceText);

                int row = i / labelsPerRow;
                int col = i % labelsPerRow;
                grid.add(labelBox, col, row);
            }

            ScrollPane scrollPane = new ScrollPane(grid);
            scrollPane.setFitToWidth(true);

            Button printButton = new Button("🖨️ طباعة");
            Button savePdfButton = new Button("📄 حفظ كـ PDF");

            HBox buttonBox = new HBox(10, printButton, savePdfButton);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setStyle("-fx-padding: 10;");

            BorderPane root = new BorderPane();
            root.setCenter(scrollPane);
            root.setBottom(buttonBox);

            Stage previewStage = new Stage();
            previewStage.setTitle("معاينة الباركود - " + productName);
            previewStage.setScene(new Scene(root, 800, 600));
            previewStage.setResizable(false);
            previewStage.show();

            printButton.setOnAction(e -> {
                PrinterJob job = PrinterJob.createPrinterJob();
                if (job != null && job.showPrintDialog(previewStage)) {
                    boolean success = job.printPage(grid);
                    if (success) job.endJob();
                }
            });

            savePdfButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("حفظ كملف PDF");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                File file = fileChooser.showSaveDialog(previewStage);

                if (file != null) {
                    saveAsPdf(grid, file.getAbsolutePath());
                }
            });

        } catch (NumberFormatException e) {
            showWarningAlert("خطأ في الإدخال", "يرجى التأكد من إدخال قيم رقمية صحيحة في الحقول.");
        } catch (Exception e) {
            showWarningAlert("خطأ", "حدث خطأ أثناء توليد الباركود: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private ImageView generateBarcodeImage(String code, int width, int height) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(code, BarcodeFormat.EAN_13, width, height);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
        ImageView imageView = new ImageView(fxImage);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private String generateEAN13(int base) {
        String raw = String.valueOf(Math.abs(base));
        raw = String.format("%012d", raw.hashCode() & 0x7fffffff).substring(0, 12);
        int checkDigit = calculateEAN13CheckDigit(raw);
        return raw + checkDigit;
    }

    private int calculateEAN13CheckDigit(String code) {
        int sum = 0;
        for (int i = 0; i < code.length(); i++) {
            int digit = Character.getNumericValue(code.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return (10 - (sum % 10)) % 10;
    }

    private void saveAsPdf(Node node, String filePath) {
        try {
            WritableImage snapshot = node.snapshot(new SnapshotParameters(), null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(new File(filePath)));
            document.open();
            Image pdfImage = Image.getInstance(bufferedImage, null);
            pdfImage.scaleToFit(500, 700);
            document.add(pdfImage);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
