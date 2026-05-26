package utils;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

public class SceneManager {

    private static Stage primaryStage;
    private static ApplicationContext springContext;

    public static void setStage(Stage stage) {
        primaryStage = stage;
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    }

    public static void setSpringContext(ApplicationContext ctx) {
        springContext = ctx;
    }

    public static ApplicationContext getSpringContext() {
        return springContext;
    }

    public static void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            if (springContext != null) {
                loader.setControllerFactory(springContext::getBean);
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
