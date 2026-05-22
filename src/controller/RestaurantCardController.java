package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.Restaurant;

public class RestaurantCardController {

    @FXML private Label nameLabel;
    @FXML private Label cuisineLabel;
    @FXML private Label ratingLabel;
    @FXML private Label ratingCountLabel;
    @FXML private Label newBadge;

    private Restaurant restaurant;

    public void setRestaurant(Restaurant r) {
        this.restaurant = r;
        nameLabel.setText(r.getName());
        cuisineLabel.setText(r.getCuisineType());
        
        if (r.getLabel() != null && r.getLabel().equalsIgnoreCase("New")) {
            newBadge.setVisible(true);
            ratingLabel.setText("★ New");
        } else {
            ratingLabel.setText(String.format("★ %.1f", r.getAvgRating()));
        }
        
        ratingCountLabel.setText("(" + r.getRatingCount() + " ratings)");
    }

    @FXML
    private void handleViewMenu() {
        utils.Session.setSelectedRestaurant(restaurant);
        utils.Session.clearCart(); // Start fresh for each restaurant
        controller.MainDashboardController.getInstance().loadViewByPath("menu_view");
    }
}
