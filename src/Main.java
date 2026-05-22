import javafx.application.Application;
import javafx.stage.Stage;
import utils.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setStage(primaryStage);
        SceneManager.switchScene("/view/login.fxml", "Online Food Ordering System - Login");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
