package ru.vsu.cvprocessing.controller;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.vsu.cvprocessing.app.Launcher;
import ru.vsu.cvprocessing.event.IRMethodChangedEvent;
import ru.vsu.cvprocessing.event.IRMethodPublisher;
import ru.vsu.cvprocessing.recognition.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import ru.vsu.cvprocessing.settings.SettingsHolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkNotNull;
import static ru.vsu.cvprocessing.recognition.ImageRecognitionMethod.BYCASCADE;
import static ru.vsu.cvprocessing.recognition.ImageRecognitionMethod.BYCOLOR;
import static ru.vsu.cvprocessing.recognition.ImageRecognitionMethod.FAKE;
import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

@Component
public class MainFormController implements Initializable {
    private static final Logger log = Logger.getLogger(MainFormController.class);

    @FXML
    private SplitPane contentSplitPane;
    @FXML
    private GridPane contentGridPane;
    @FXML
    private ScrollPane recognitionSettingPane;
    @FXML
    private ImageView cameraImageView;
    @FXML
    private CheckBox sendDetectionDataCheckBox;

    @Autowired
    private IRMethodPublisher irMethodPublisher;

    private Stage settingsStage = null;
    private boolean fixShownSelectedPixelColor = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sendDetectionDataCheckBox.selectedProperty().bindBidirectional(getInstance().sendDetectionDataProperty());

        Platform.runLater(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                        .getResource(SettingsHolder.FXML_FILE_PREF + "settings.fxml"));
                fxmlLoader.setControllerFactory(Launcher.springContext::getBean);
                Parent root = fxmlLoader.load();
                settingsStage = new Stage();
                settingsStage.setTitle("Settings");
                settingsStage.setResizable(false);
                settingsStage.setScene(new Scene(root));
            } catch (IOException e) {
                log.error(e);
            }
        });

        cameraImageView.setPreserveRatio(true);
        setImageViewDimension(cameraImageView, 340d, 495d);

        contentSplitPane.getDividers().get(0).positionProperty().addListener(observable -> {
            if (contentGridPane.getHeight() > 0 && contentGridPane.getWidth() > 0) {
                setImageViewDimension(cameraImageView, contentGridPane.getHeight(), contentGridPane.getWidth());
            }
        });

        irMethodPublisher.publish(new IRMethodChangedEvent(this, null, BYCOLOR));
    }

    @FXML
    private void handleOpenSettings() {
        if (settingsStage.isShowing()) {
            settingsStage.requestFocus();
        } else {
            //TODO "smart" positioning
            settingsStage.show();
        }
    }

    @FXML
    public void handleClose() {
        settingsStage.close();
        Platform.exit();
    }

    /* Handles for switch recognition type */
    private void handleSwitchToFake() throws Exception {
        getInstance().getImageRecognition().closeVideoCapture();
        log.info("Video capture for image recognition closed");

        getInstance().setImageRecognition(new FakeImageRecognition() {{
            setCamera(getInstance().getCamera());
        }});

        recognitionSettingPane.setContent(FXMLLoader.load(getClass()
                .getResource(SettingsHolder.FXML_FILE_PREF + "irfake.fxml")));

        getInstance().getImageRecognition().openVideoCapture(new HashMap<String, Object>() {{
            put("viewCamera", cameraImageView);
        }});
        log.info(String.format("Video capture for image recognition method %s opened", FAKE));
    }

    private void handleSwitchToRecognizeByColor() throws Exception {
        getInstance().getImageRecognition().closeVideoCapture();
        log.info("Video capture for image recognition closed");
        getInstance().setImageRecognition(new RecognizeByColor() {{
            setCamera(getInstance().getCamera());
        }});

        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource(SettingsHolder.FXML_FILE_PREF + "irbycolor.fxml"));
        fxmlLoader.setControllerFactory(getInstance().getApplicationContext()::getBean);
        recognitionSettingPane.setContent(fxmlLoader.load());

        IRByColorController irByColorController = fxmlLoader.getController();
        getInstance().getImageRecognition().openVideoCapture(new HashMap<String, Object>() {{
            put("viewCamera", cameraImageView);
            put("viewMaskImage", irByColorController.getMaskImageView());
            put("viewMorphImage", irByColorController.getMorphImageView());
        }});
        log.info(String.format("Video capture for image recognition method %s opened", ImageRecognitionMethod.BYCOLOR));
    }

    private void handleSwitchToRecognizeByCascade() throws Exception {
        getInstance().getImageRecognition().closeVideoCapture();
        log.info("Video capture for image recognition closed");

        if (getInstance().getHaarCascadeConfigFilename() == null) {
            File haarCascadesDirectory = new File(getClass().getResource(SettingsHolder.CASCADE_FILE_PREF).getPath());
            getInstance().setHaarCascadeConfigFilename(checkNotNull(haarCascadesDirectory.listFiles())[4].getName());
        }

        String filePath = getClass().getResource(SettingsHolder.CASCADE_FILE_PREF + getInstance().getHaarCascadeConfigFilename()).getPath();
        getInstance().setImageRecognition(new RecognizeByCascade(filePath) {{
            setCamera(getInstance().getCamera());
        }});

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(SettingsHolder.FXML_FILE_PREF + "irbycascade.fxml"));
        fxmlLoader.setControllerFactory(Launcher.springContext::getBean);
        recognitionSettingPane.setContent(fxmlLoader.load());

        getInstance().getImageRecognition().openVideoCapture(new HashMap<String, Object>() {{
            put("viewCamera", cameraImageView);
        }});
        log.info(String.format("Video capture for image recognition method %s opened", ImageRecognitionMethod.BYCASCADE));
    }
    /* Handles for switch recognition type */

    /* Event publishing and handling */
    @FXML
    private void handleCameraImageViewClick() {
        fixShownSelectedPixelColor = !fixShownSelectedPixelColor;
    }

    @FXML
    private void handleChangeIRFakeClick() {
        irMethodPublisher.publish(new IRMethodChangedEvent(this, null, FAKE));
    }

    @FXML
    private void handleChangeIRByColorClick() {
        irMethodPublisher.publish(new IRMethodChangedEvent(this, null, BYCOLOR));
    }

    @FXML
    private void handleChangeIRByCascadeClick() {
        irMethodPublisher.publish(new IRMethodChangedEvent(this, null, BYCASCADE));
    }

    @EventListener
    public void handleChangeIRMethod(IRMethodChangedEvent event) {
        try {
            StringBuilder logMessage = new StringBuilder("Image recognition method was");
            logMessage.append(event.getOldValue() == null ? " set " : " changed from " + event.getOldValue());
            logMessage.append(" to ").append(event.getNewValue());
            log.info(logMessage);
            switch (event.getNewValue()) {
                case FAKE:
                    handleSwitchToFake();
                    break;
                case BYCOLOR:
                    handleSwitchToRecognizeByColor();
                    break;
                case BYCASCADE:
                    handleSwitchToRecognizeByCascade();
                    break;
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
    /* Event publishing and handling */

    /* Static methods */
    private static void setImageViewDimension(ImageView imageView, double height, double width) {
        imageView.setFitHeight(height);
        imageView.setFitWidth(width);
    }
    /* Static methods */
}
