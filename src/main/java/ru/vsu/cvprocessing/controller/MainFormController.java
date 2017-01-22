package ru.vsu.cvprocessing.controller;

import ru.vsu.cvprocessing.holder.Camera;
import ru.vsu.cvprocessing.holder.CameraHolder;
import ru.vsu.cvprocessing.holder.ServoMotorControl;
import ru.vsu.cvprocessing.recognition.FakeImageRecognition;
import ru.vsu.cvprocessing.recognition.ImageRecognition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainFormController implements Initializable {
    private static final Logger log = Logger.getLogger(MainFormController.class);

    @FXML
    private Pane recognitionSettingPane;
    @FXML
    private ImageView cameraImageView;
    @FXML
    private Button openSettingsButton;

    private Stage settingsStage = null;

    private Camera camera;
    private CameraHolder cameraHolder;
    private ImageRecognition imageRecognition;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("../../../../fxml/settings.fxml"));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        settingsStage = new Stage();
        settingsStage.setTitle("Title");
        settingsStage.setResizable(false);
        settingsStage.setScene(new Scene(root));

        cameraImageView.setPreserveRatio(true);
        camera = new Camera(0, 70, 400, 600);

        cameraHolder = new ServoMotorControl(camera);
        imageRecognition = new FakeImageRecognition(camera);

        openSettingsButton.setOnAction(event -> handleOpenSettings());

//        SettingsHolder.getInstance().switchToFake(camera, cameraImageView);
//        SettingsHolder.getInstance().switchToRecognizeByColor(camera, cameraImageView, null, null);
    }

    private void handleOpenSettings() {
        if (settingsStage.isShowing()) {
            settingsStage.requestFocus();
        } else {
            //TODO smart position
            settingsStage.show();
        }
    }

    public void handleClose() {
        settingsStage.close();// TODO call setting form ru.vsu.cvprocessing.controller's close method
        cameraHolder.closeConnection();
        imageRecognition.closeVideoCapture();
    }
}
