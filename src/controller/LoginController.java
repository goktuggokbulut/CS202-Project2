package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;
import service.UserService;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User user = userService.login(username, password);

        if (user != null) {
            utils.Session.login(user);
            utils.SceneManager.switchScene("/view/main_dashboard.fxml", "Foodly - Dashboard");
        } else {
            messageLabel.setText("Invalid username or password.");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleRegister() {
        utils.SceneManager.switchScene("/view/registration.fxml", "Foodly - Create Account");
    }
}
