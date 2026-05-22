package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.MenuItem;
import utils.Session;

import java.text.NumberFormat;
import java.util.Locale;

public class MenuItemCardController {

    @FXML private Label nameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priceLabel;

    private MenuItem item;
    private Runnable onAddToCart;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public void setItem(MenuItem item, Runnable onAddToCart) {
        this.item = item;
        this.onAddToCart = onAddToCart;
        nameLabel.setText(item.getName());
        descriptionLabel.setText(item.getDescription());
        priceLabel.setText(currencyFormat.format(item.getPrice()));
    }

    @FXML
    private void handleAddToCart() {
        Session.addToCart(item);
        if (onAddToCart != null) {
            onAddToCart.run();
        }
    }
}
