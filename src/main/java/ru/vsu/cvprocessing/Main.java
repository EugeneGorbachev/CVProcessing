package ru.vsu.cvprocessing;

import ru.vsu.cvprocessing.controller.MainFormController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;

public class Main extends Application {
    public void start(Stage primaryStage) throws Exception {
        String fxmlFile = "../../../fxml/mainform.fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = fxmlLoader.load();

        primaryStage.setTitle("CV image processing");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinHeight(420d);
        primaryStage.setMinWidth(600d);
        primaryStage.show();

        MainFormController mc = fxmlLoader.getController();
        primaryStage.setOnCloseRequest(event -> mc.handleClose());
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
