package app.ui;

import app.dao.DatabaseInitializer;
import app.dao.SqliteResultDao;
import app.service.ResultService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseInitializer.initialize();

        ResultService service = new ResultService(new SqliteResultDao());

        URL fxmlLocation = getClass().getResource("/views/ResultView.fxml");
        if (fxmlLocation == null) {
            fxmlLocation = getClass().getClassLoader().getResource("views/ResultView.fxml");
        }

        if (fxmlLocation == null) {
            throw new IllegalStateException(
                "Cannot find ResultView.fxml in resources! Please verify file location under src/main/resources/views."
            );
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        ResultViewController controller = loader.getController();
        controller.initData(service);

        primaryStage.setTitle("Departmental Result System - CSC 508");
        primaryStage.setScene(new Scene(root, 880, 560));
        primaryStage.setMinWidth(750);
        primaryStage.setMinHeight(480);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
