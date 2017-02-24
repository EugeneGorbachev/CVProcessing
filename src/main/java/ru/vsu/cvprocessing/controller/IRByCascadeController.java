package ru.vsu.cvprocessing.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.vsu.cvprocessing.event.IRMethodChangedEvent;
import ru.vsu.cvprocessing.event.IRMethodPublisher;
import ru.vsu.cvprocessing.settings.SettingsHolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static ru.vsu.cvprocessing.recognition.ImageRecognitionMethod.*;
import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

@Component
public class IRByCascadeController implements Initializable {
    private static final Logger log = Logger.getLogger(IRByCascadeController.class);

    @FXML
    private ChoiceBox haarCascadeChooseBox;
    @FXML
    private TextArea previewHaarCascadeTextArea;

    @Autowired
    private IRMethodPublisher irMethodPublisher;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File haarCascadesDirectory = new File(getClass().getResource(SettingsHolder.CASCADE_FILE_PREF).getPath());

        haarCascadeChooseBox.setItems(FXCollections.observableArrayList(
                Stream.of(checkNotNull(haarCascadesDirectory.listFiles()))
                        .map(file -> file.getName())
                        .collect(Collectors.toList())
        ));

        haarCascadeChooseBox.getSelectionModel().select(getInstance().getHaarCascadeConfigFilename());
        previewHaarCascade(haarCascadesDirectory, getInstance().getHaarCascadeConfigFilename());
        haarCascadeChooseBox.valueProperty().addListener(((observable, oldValue, newValue) -> {
            getInstance().setHaarCascadeConfigFilename((String) newValue);
            log.info("Haar cascade config filename value was changed from \"" + oldValue + "\" to \"" + newValue + "\"");
            irMethodPublisher.publish(new IRMethodChangedEvent(this, null, BYCASCADE));
        }));
    }

    private void previewHaarCascade(File haarCascadesDirectory, String filename) {
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(haarCascadesDirectory.getAbsolutePath() + "/" + filename)
            );

            String line;
            previewHaarCascadeTextArea.clear();
            for (int i = 0; (line = bufferedReader.readLine()) != null && i < 100; i++) {
                previewHaarCascadeTextArea.appendText(line + "\n");
            }
            if (line != null) {
                previewHaarCascadeTextArea.appendText("...");
            }
            previewHaarCascadeTextArea.positionCaret(1);
        } catch (IOException e) {
            log.error(e);
        }
    }
}