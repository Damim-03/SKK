package com.example.library.controller.home;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Objects;
import static com.example.library.Alert.alert.showFailedAlert;
import static com.example.library.Alert.alert.showWarningAlert;

public class LoadingPageController {

    @FXML
    private Rectangle progressBar;
    @FXML
    private Rectangle backgroundRectangle;
    @FXML
    private Label percentageLabel;

    private double maxWidth;

    @FXML
    public void initialize() {
        // Validate FXML elements
        if (backgroundRectangle == null || progressBar == null || percentageLabel == null) {
            showWarningAlert("خطأ", "الصورة غير موجودة.");
            return;
        }

        // Initialize progress bar
        maxWidth = backgroundRectangle.getWidth(); // Dynamically get max width
        progressBar.setWidth(0); // Start at 0

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        KeyFrame keyFrame = new KeyFrame(Duration.millis(50), e -> {
            double width = progressBar.getWidth();
            if (width < maxWidth) {
                double newWidth = Math.min(maxWidth, width + 5); // Prevent overshooting
                progressBar.setWidth(newWidth);
                double percent = Math.min(100.0, (newWidth / maxWidth) * 100); // Cap at 100%
                percentageLabel.setText(String.format("%.0f%%", percent));
            } else {
                timeline.stop();
                percentageLabel.setText("100%"); // Ensure final value is 100%
                HomeScreen();
            }
        });

        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    @FXML
    private void HomeScreen() {
        try {
            // Load the FXML file for the home screen
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/home/home.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage for the home screen
            javafx.stage.Stage homeStage = new javafx.stage.Stage();
            homeStage.setTitle("Soubirate Kamel Kir");
            homeStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the home stage
            homeStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/SKK-1.png"))));

            // Show the home screen

            homeStage.setMaximized(true);

            homeStage.show();

            // Close the loading page stage
            Stage loadingStage = (Stage) progressBar.getScene().getWindow();
            loadingStage.close();
        } catch (Exception e) {
            showFailedAlert("خطأ", "خطأ في فتح شاشة الرئيسية");
        }
    }
}