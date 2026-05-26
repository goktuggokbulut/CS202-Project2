package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.MenuItem;
import model.OrderItem;
import model.Restaurant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import repository.MenuRepository;
import utils.SceneManager;
import utils.Session;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class MenuViewController {

    @FXML private Label restaurantNameLabel;
    @FXML private Label cuisineLabel;
    @FXML private VBox menuContainer;
    @FXML private VBox cartItemsBox;
    @FXML private Label totalLabel;

    @Autowired private MenuRepository menuRepository;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    @FXML
    public void initialize() {
        Restaurant r = Session.getSelectedRestaurant();
        if (r != null) {
            restaurantNameLabel.setText(r.getName());
            cuisineLabel.setText(r.getCuisineType());
            loadMenu(r.getRestaurantId());
        }
        updateCartUI();
    }

    private void loadMenu(int restaurantId) {
        menuContainer.getChildren().clear();
        List<MenuItem> items = menuRepository.getMenuByRestaurant(restaurantId);

        Map<String, List<MenuItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getCategoryName() != null ? i.getCategoryName() : "Other",
                        LinkedHashMap::new,
                        Collectors.toList()));

        for (Map.Entry<String, List<MenuItem>> entry : grouped.entrySet()) {
            VBox categoryBox = new VBox(10);
            Label catLabel = new Label(entry.getKey());
            catLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

            FlowPane itemPane = new FlowPane(15, 15);

            for (MenuItem item : entry.getValue()) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menu_item_card.fxml"));
                    if (SceneManager.getSpringContext() != null) {
                        loader.setControllerFactory(SceneManager.getSpringContext()::getBean);
                    }
                    Parent card = loader.load();
                    MenuItemCardController ctrl = loader.getController();
                    ctrl.setItem(item, this::updateCartUI);
                    itemPane.getChildren().add(card);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            categoryBox.getChildren().addAll(catLabel, itemPane);
            menuContainer.getChildren().add(categoryBox);
        }
    }

    private void updateCartUI() {
        cartItemsBox.getChildren().clear();
        List<OrderItem> cart = Session.getCart();

        for (OrderItem oi : cart) {
            HBox row = new HBox(6);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #f6f8fa; -fx-background-radius: 6; -fx-padding: 6 8;");

            Label nameLabel = new Label(oi.getItemName());
            nameLabel.setStyle("-fx-font-size: 12px;");
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            nameLabel.setMaxWidth(Double.MAX_VALUE);

            Button minusBtn = new Button("−");
            minusBtn.getStyleClass().addAll("btn-sm", "flat");
            minusBtn.setOnAction(e -> { Session.decreaseFromCart(oi.getItemId()); updateCartUI(); });

            Label qtyLabel = new Label(String.valueOf(oi.getQuantity()));
            qtyLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 18; -fx-alignment: CENTER;");

            Button plusBtn = new Button("+");
            plusBtn.getStyleClass().addAll("btn-sm", "flat");
            plusBtn.setOnAction(e -> { Session.addToCart(oi.toMenuItem()); updateCartUI(); });

            Button removeBtn = new Button("×");
            removeBtn.getStyleClass().addAll("btn-sm", "danger");
            removeBtn.setOnAction(e -> { Session.removeFromCart(oi.getItemId()); updateCartUI(); });

            row.getChildren().addAll(nameLabel, minusBtn, qtyLabel, plusBtn, removeBtn);
            cartItemsBox.getChildren().add(row);
        }

        BigDecimal total = cart.stream()
                .map(oi -> oi.getUnitPrice().multiply(new BigDecimal(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText(currencyFormat.format(total));
    }

    @FXML
    private void handleBack() {
        MainDashboardController.getInstance().loadViewByPath("restaurant_search");
    }

    @FXML
    private void handleCheckout() {
        if (Session.getCart().isEmpty()) {
            System.out.println("Cart is empty!");
            return;
        }
        MainDashboardController.getInstance().loadViewByPath("checkout_view");
    }
}
