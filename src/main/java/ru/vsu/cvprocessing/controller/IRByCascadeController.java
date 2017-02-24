package ru.vsu.cvprocessing.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import org.apache.log4j.Logger;
import ru.vsu.cvprocessing.settings.SettingsHolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkNotNull;
import static ru.vsu.cvprocessing.settings.SettingsHolder.getInstance;

public class IRByCascadeController implements Initializable {
    private static final Logger log = Logger.getLogger(IRByCascadeController.class);

    @FXML
    private ChoiceBox haarCascadeChooseBox;
    @FXML
    private TextArea previewHaarCascadeTextArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<String> haarCascades = new ArrayList<>();
        File haarCascadesDirectory = new File(
                getClass().getResource(SettingsHolder.CASCADE_FILE_PREF).getPath());

        for (File file : checkNotNull(haarCascadesDirectory.listFiles())) {
            haarCascades.add(file.getName());
        }
        haarCascadeChooseBox.setItems(FXCollections.observableArrayList(haarCascades));
        haarCascadeChooseBox.getSelectionModel().select(haarCascades.indexOf(getInstance().getHaarCascadeConfigFilename()));
        haarCascadeChooseBox.valueProperty().addListener(((observable, oldValue, newValue) -> {
            handleChangeCascade(haarCascadesDirectory, (String) oldValue, (String) newValue);
            //TODO event change irmethod
        }));
    }

    private void handleChangeCascade(File haarCascadesDirectory, String oldValue, String newValue) {
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(haarCascadesDirectory.getAbsolutePath() + "/" + newValue)
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
        getInstance().setHaarCascadeConfigFilename(newValue);
        log.info("Haar cascade config filename value was changed from \"" + oldValue + "\" to \"" + newValue + "\"");
    }
}