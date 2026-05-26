package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import service.UserService;
import utils.SceneManager;

@Component
@Scope("prototype")
public class RegistrationController {

    @FXML private RadioButton customerRadio;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField cityField;
    @FXML private TextField phoneField;
    @FXML private ListView<String> phoneListView;
    @FXML private TextField addressField;
    @FXML private ListView<String> addressListView;
    @FXML private Label messageLabel;

    private final ObservableList<String> phones = FXCollections.observableArrayList();
    private final ObservableList<String> addresses = FXCollections.observableArrayList();

    @Autowired private UserService userService;

    @FXML
    public void initialize() {
        phoneListView.setItems(phones);
        addressListView.setItems(addresses);
    }

    @FXML
    private void addPhone() {
        String p = phoneField.getText().trim();
        if (!p.isEmpty() && !phones.contains(p)) {
            phones.add(p);
            phoneField.clear();
        }
    }

    @FXML
    private void addAddress() {
        String a = addressField.getText().trim();
        if (!a.isEmpty() && !addresses.contains(a)) {
            addresses.add(a);
            addressField.clear();
        }
    }

    @FXML
    private void handleRegister() {
        User user = new User();
        user.setUsername(usernameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setPassword(passwordField.getText().trim());
        user.setCity(cityField.getText().trim());

        String type = customerRadio.isSelected() ? "Customer" : "RestaurantManager";

        if (userService.register(user, type, addresses, phones)) {
            messageLabel.setText("Account created successfully!");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
        } else {
            messageLabel.setText("Registration failed. Check if username exists.");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/view/login.fxml", "Login");
    }
}
