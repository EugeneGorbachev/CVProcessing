package ru.vsu.cvprocessing.controller;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import org.springframework.context.event.EventListener;
import ru.vsu.cvprocessing.event.ChangeIRMethodEvent;
import ru.vsu.cvprocessing.recognition.FakeImageRecognition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import ru.vsu.cvprocessing.recognition.ImageRecognitionMethod;
import ru.vsu.cvprocessing.recognition.RecognizeByCascade;
import ru.vsu.cvprocessing.recognition.RecognizeByColor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkNotNull;
import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

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
    private Button openSettingsButton;

    private Stage settingsStage = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cameraImageView.setPreserveRatio(true);
        setImageViewDimension(cameraImageView, getInstance().getCamera().getHeight(), getInstance().getCamera().getWidth());

        contentSplitPane.getDividers().get(0).positionProperty().addListener(
                observable -> {
                    if (contentGridPane.getHeight() > 0 && contentGridPane.getWidth() > 0) {
                        setImageViewDimension(cameraImageView, contentGridPane.getHeight(), contentGridPane.getWidth());
                    }
                });

        openSettingsButton.setOnAction(event -> handleOpenSettings());

        //TODO remove this
        handleChangeIRMethod(new ChangeIRMethodEvent(this, ImageRecognitionMethod.FAKE, ImageRecognitionMethod.BYCOLOR));
    }

    private void handleOpenSettings() {
        if (settingsStage == null) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("../../../../fxml/settings.fxml"));
                settingsStage = new Stage();
                settingsStage.setTitle("Settings");
                settingsStage.setResizable(false);
                settingsStage.setScene(new Scene(root));
            } catch (IOException e) {
                log.error(e);
            }
        }

        if (settingsStage.isShowing()) {
            settingsStage.requestFocus();
        } else {
            //TODO smart position
            settingsStage.show();
        }
    }

    public void handleClose() {
        getInstance().getCameraHolder().closeConnection();
        getInstance().getImageRecognition().closeVideoCapture();
    }

    /* Handles for switch recognition type */
    private void handleSwitchToNone() throws Exception {
        getInstance().getImageRecognition().closeVideoCapture();
        getInstance().setImageRecognition(new FakeImageRecognition(getInstance().getCamera()));

        recognitionSettingPane.setContent(FXMLLoader.load(getClass().getResource("../../../../fxml/irfake.fxml")));

        getInstance().getImageRecognition().openVideoCapture(new HashMap<String, Object>() {{
            put("viewCamera", cameraImageView);
        }});
    }

    private void handleSwitchToRecognizeByColor() throws Exception {
        getInstance().getImageRecognition().closeVideoCapture();
        getInstance().setImageRecognition(new RecognizeByColor(getInstance().getCamera()));

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../../../fxml/irbycolor.fxml"));
        recognitionSettingPane.setContent(fxmlLoader.load());

        IRByColorController irByColorController = fxmlLoader.getController();
        getInstance().getImageRecognition().openVideoCapture(new HashMap<String, Object>() {{
            put("viewCamera", cameraImageView);
            put("viewMaskImage", irByColorController.getMaskImageView());
            put("viewMorphImage", irByColorController.getMorphImageView());
        }});
    }

    private void handleSwitchToRecognizeByCascade() throws Exception {
        getInstance().getImageRecognition().closeVideoCapture();

        if (getInstance().getHaarCascadeConfigFilename() == null) {
            File haarCascadesDirectory = new File(getClass().getResource("../../../../haarcascades").getPath());
            getInstance().setHaarCascadeConfigFilename(checkNotNull(haarCascadesDirectory.listFiles())[0].getName());
        }
        getInstance().setImageRecognition(new RecognizeByCascade(
                getInstance().getCamera(),
                getClass().getResource("../../../../haarcascades/" + getInstance().getHaarCascadeConfigFilename()).getPath())
        );

        recognitionSettingPane.setContent(FXMLLoader.load(getClass().getResource("../../../../fxml/irbycascade.fxml")));

        getInstance().getImageRecognition().openVideoCapture(new HashMap<String, Object>() {{
            put("viewCamera", cameraImageView);
        }});
    }
    /* Handles for switch recognition type */

    @EventListener
    public void handleChangeIRMethod(ChangeIRMethodEvent event) {
        try {
            switch (event.getNewValue()) {
                case FAKE:
                    handleSwitchToNone();
                    break;
                case BYCOLOR:
                    handleSwitchToRecognizeByColor();
                    break;
                case BYCASCADE:
                    handleSwitchToRecognizeByCascade();
                    break;
            }
            log.info("Image recognition method was changed from \"" + event.getOldValue() + "\" to \"" + event.getNewValue() + "\"");
        } catch (Exception e) {
            log.error(e);
        }
    }

    /* Static methods */
    private static void setImageViewDimension(ImageView imageView, double height, double width) {
        imageView.setFitHeight(height);
        imageView.setFitWidth(width);
    }
    /* Static methods */
}
