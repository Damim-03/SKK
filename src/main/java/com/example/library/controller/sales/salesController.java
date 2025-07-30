package com.example.library.controller.sales;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

public class salesController {

    @FXML
    private TabPane tabPane;

    @FXML
    public void initialize() {
        System.out.println("initialize called: tabPane = " + tabPane);

        // Prevent closing the "+" tab
        Tab plusTab = tabPane.getTabs().get(tabPane.getTabs().size() - 1);
        plusTab.setClosable(false);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && "+".equals(newTab.getText())) {
                Tab newTabScreen = new Tab("شاشة جديدة");
                AnchorPane content = new AnchorPane();
                newTabScreen.setContent(content);
                newTabScreen.setClosable(true);

                tabPane.getTabs().add(tabPane.getTabs().size() - 1, newTabScreen);
                tabPane.getSelectionModel().select(newTabScreen);
            }
        });
    }
}
