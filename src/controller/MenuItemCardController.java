package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.MenuItem;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import utils.Session;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

@Component
@Scope("prototype")
public class MenuItemCardController {

    @FXML private ImageView itemImage;
    @FXML private ImageView imagePlaceholder;
    @FXML private Label     nameLabel;
    @FXML private Label     descriptionLabel;
    @FXML private Label     priceLabel;

    private MenuItem item;
    private Runnable onAddToCart;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    public void setItem(MenuItem item, Runnable onAddToCart) {
        this.item = item;
        this.onAddToCart = onAddToCart;
        nameLabel.setText(item.getName());
        descriptionLabel.setText(item.getDescription());
        priceLabel.setText(currencyFormat.format(item.getPrice()));
        loadImage(item.getImageUrl());
    }

    private void loadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            showPlaceholder();
            return;
        }
        try {
            URL resource = getClass().getResource("/" + imageUrl);
            String urlStr = resource != null ? resource.toExternalForm() : imageUrl;

            Image img = new Image(urlStr, 280, 150, false, true, true);
            img.errorProperty().addListener((obs, wasError, isError) -> {
                if (isError) showPlaceholder();
            });
            if (img.isError()) {
                showPlaceholder();
            } else {
                itemImage.setImage(img);
                imagePlaceholder.setVisible(false);
            }
        } catch (Exception e) {
            showPlaceholder();
        }
    }

    private void showPlaceholder() {
        itemImage.setImage(null);
        if (imagePlaceholder.getImage() == null) {
            java.net.URL ph = getClass().getResource("/img/food_placeholder.jpg");
            if (ph != null) imagePlaceholder.setImage(new Image(ph.toExternalForm(), 280, 150, false, true));
        }
        imagePlaceholder.setVisible(true);
    }

    @FXML
    private void handleAddToCart() {
        Session.addToCart(item);
        if (onAddToCart != null) {
            onAddToCart.run();
        }
    }
}
