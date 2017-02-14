package ru.vsu.cvprocessing.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;
import ru.vsu.cvprocessing.controller.MainFormController;
import ru.vsu.cvprocessing.settings.SettingsHolder;
import ru.vsu.cvprocessing.settings.SpringConfig;

import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

@SpringBootApplication
public class Launcher extends Application {
    public static ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {
        springContext = SpringApplication.run(SpringConfig.class);
        SettingsHolder.getInstance().setApplicationContext(springContext);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(SettingsHolder.FXML_FILE_PREF + "mainform.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);
        Parent root = fxmlLoader.load();

        primaryStage.setTitle("CV image processing");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinHeight(420d);
        primaryStage.setMinWidth(600d);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> ((MainFormController) fxmlLoader.getController()).handleClose());
    }

    @Override
    public void stop() throws Exception {
        getInstance().getImageRecognition().closeVideoCapture();
        getInstance().getCameraHolder().closeConnection();
        springContext.stop();
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
