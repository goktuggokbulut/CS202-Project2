package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import model.Restaurant;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import repository.RestaurantRepository;
import utils.SceneManager;
import utils.Session;

import java.io.IOException;
import java.util.List;

@Component
@Scope("prototype")
public class RestaurantSearchController {

    @FXML private Label cityLabel;
    @FXML private TextField searchField;
    @FXML private FlowPane resultsPane;

    @Autowired private RestaurantRepository restaurantRepository;

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();
        if (user != null) {
            cityLabel.setText("Showing restaurants in " + user.getCity());
            loadRestaurants("", user.getCity());
        }
    }

    @FXML
    private void handleSearch() {
        User user = Session.getCurrentUser();
        if (user != null) {
            loadRestaurants(searchField.getText().trim(), user.getCity());
        }
    }

    private void loadRestaurants(String keyword, String city) {
        resultsPane.getChildren().clear();
        List<Restaurant> list = restaurantRepository.searchByCityAndKeyword(city, keyword);

        for (Restaurant r : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/restaurant_card.fxml"));
                if (SceneManager.getSpringContext() != null) {
                    loader.setControllerFactory(SceneManager.getSpringContext()::getBean);
                }
                Parent card = loader.load();

                RestaurantCardController ctrl = loader.getController();
                ctrl.setRestaurant(r);

                resultsPane.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
