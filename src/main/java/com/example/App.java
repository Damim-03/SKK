package com.example;

import com.example.library.util.LicenseValidator; // 👈 أضف هذا الاستيراد
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // ✅ تحقق من الترخيص أولاً
        if (!LicenseValidator.isValid()) {
            // يمكنك هنا إما إظهار نافذة تنبيه (Alert) أو إغلاق البرنامج
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("ترخيص غير صالح");
            alert.setHeaderText(null);
            alert.setContentText("⚠️ هذا التطبيق غير مرخّص للعمل على هذا الجهاز.");
            alert.showAndWait();
            System.exit(0);
        }

        // ✅ إذا الترخيص صحيح، كمل عادي
        scene = new Scene(loadFXML("interfaces/home/loading_Page"));
        stage.setScene(scene);

        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/SKK-1.png")));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Warning: Could not load window icon");
        }

        stage.setTitle("Soubirate Kamel Kir");
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
