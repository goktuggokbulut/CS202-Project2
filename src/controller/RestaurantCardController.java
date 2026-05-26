package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Restaurant;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
@Scope("prototype")
public class RestaurantCardController {

    @FXML private Label     nameLabel;
    @FXML private Label     cuisineLabel;
    @FXML private Label     cityLabel;
    @FXML private Label     ratingLabel;
    @FXML private Label     ratingCountLabel;
    @FXML private Label     newBadge;
    @FXML private ImageView coverImage;

    private Restaurant restaurant;

    public void setRestaurant(Restaurant r) {
        this.restaurant = r;
        nameLabel.setText(r.getName());
        cuisineLabel.setText(r.getCuisineType());
        cityLabel.setText(r.getCity());

        if (r.getLabel() != null && r.getLabel().equalsIgnoreCase("New")) {
            newBadge.setVisible(true);
            ratingLabel.setText("★ New");
        } else {
            ratingLabel.setText(String.format("★ %.1f", r.getAvgRating()));
        }

        ratingCountLabel.setText("(" + r.getRatingCount() + " ratings)");
        loadCoverImage();
    }

    private void loadCoverImage() {
        String[] candidates = {
            "/img/restaurants/" + restaurant.getName().toLowerCase().replaceAll("\\s+", "_") + ".jpg",
            "/img/restaurant_default.jpg"
        };
        for (String path : candidates) {
            URL resource = getClass().getResource(path);
            if (resource != null) {
                try {
                    Image img = new Image(resource.toExternalForm(), 320, 160, false, true, true);
                    img.errorProperty().addListener((obs, was, isErr) -> {
                        if (isErr) coverImage.setImage(null);
                    });
                    if (!img.isError()) {
                        coverImage.setImage(img);
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    @FXML
    private void handleViewMenu() {
        utils.Session.setSelectedRestaurant(restaurant);
        utils.Session.clearCart();
        controller.MainDashboardController.getInstance().loadViewByPath("menu_view");
    }
}
