package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.MenuItem;
import utils.Session;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

public class MenuItemCardController {

    @FXML private ImageView itemImage;
    @FXML private Label     imagePlaceholder;
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
            // First try as a classpath resource (e.g. "img/hummus.jpg")
            URL resource = getClass().getResource("/" + imageUrl);
            String urlStr = resource != null ? resource.toExternalForm() : imageUrl;

            Image img = new Image(urlStr, 280, 150, false, true, true);  // background load
            img.errorProperty().addListener((obs, wasError, isError) -> {
                if (isError) showPlaceholder();
            });
            // If already loaded synchronously and errored
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
