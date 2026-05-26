package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import repository.UserRepository;
import utils.Session;

import java.util.List;

@Component
@Scope("prototype")
public class ProfileController {

    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label cityLabel;
    @FXML private Label typeLabel;
    @FXML private ListView<String> addressListView;
    @FXML private ListView<String> phoneListView;

    @Autowired private UserRepository userRepository;

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();
        if (user != null) {
            usernameLabel.setText(user.getUsername());
            emailLabel.setText(user.getEmail());
            cityLabel.setText(user.getCity());
            typeLabel.setText(user.getUserType());

            List<String> addresses = userRepository.getAddresses(user.getUsername());
            addressListView.setItems(FXCollections.observableArrayList(addresses));

            List<String> phones = userRepository.getPhones(user.getUsername());
            phoneListView.setItems(FXCollections.observableArrayList(phones));
        }
    }
}
