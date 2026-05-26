package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import model.Restaurant;
import repository.RestaurantRepository;
import utils.Session;

import java.util.List;

public class ManagerOverviewController {

    @FXML private ComboBox<Restaurant> restaurantSelector;
    @FXML private Label nameLabel;
    @FXML private Label cuisineLabel;
    @FXML private Label cityLabel;
    @FXML private Label addressLabel;
    @FXML private Label ratingLabel;
    @FXML private Label ratingCountLabel;

    private final RestaurantRepository restaurantRepository = new RestaurantRepository();

    @FXML
    public void initialize() {
        String managerUsername = Session.getCurrentUser().getUsername();
        List<Restaurant> restaurants = restaurantRepository.getRestaurantsByManager(managerUsername);

        restaurantSelector.setItems(FXCollections.observableArrayList(restaurants));
        restaurantSelector.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> { if (newVal != null) populateDetails(newVal); }
        );

        if (!restaurants.isEmpty()) {
            restaurantSelector.getSelectionModel().selectFirst();
        }
    }

    private void populateDetails(Restaurant r) {
        nameLabel.setText(r.getName());
        cuisineLabel.setText(r.getCuisineType());
        cityLabel.setText(r.getCity());
        addressLabel.setText(r.getAddress());

        if ("New".equals(r.getLabel())) {
            ratingLabel.setText("★ New (fewer than 10 ratings)");
            ratingLabel.setStyle("-fx-text-fill: #9a6700;");
        } else {
            ratingLabel.setText(String.format("★ %.1f / 5.0", r.getAvgRating()));
            ratingLabel.setStyle("-fx-text-fill: #1a7f37;");
        }
        ratingCountLabel.setText(r.getRatingCount() + " rating(s) received");
    }
}
