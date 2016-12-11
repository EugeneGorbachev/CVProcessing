import controllers.CVFormController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;

public class Main extends Application {

    public void start(Stage primaryStage) throws Exception {
        String fxmlFile = "fxml/cvform.fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = fxmlLoader.load();

        primaryStage.setTitle("Title");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        CVFormController cv = fxmlLoader.getController();
        primaryStage.setOnCloseRequest(event -> cv.handleClose());
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
