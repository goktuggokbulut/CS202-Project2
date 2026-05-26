package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import model.MenuItem;
import model.OrderItem;
import model.Restaurant;
import repository.MenuRepository;
import utils.Session;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MenuViewController {

    @FXML private Label restaurantNameLabel;
    @FXML private Label cuisineLabel;
    @FXML private VBox menuContainer;
    @FXML private ListView<String> cartListView;
    @FXML private Label totalLabel;

    private final MenuRepository menuRepository = new MenuRepository();
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

        // Group by category
        Map<String, List<MenuItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(MenuItem::getCategoryName, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<MenuItem>> entry : grouped.entrySet()) {
            VBox categoryBox = new VBox(10);
            Label catLabel = new Label(entry.getKey());
            catLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");
            
            FlowPane itemPane = new FlowPane(15, 15);
            
            for (MenuItem item : entry.getValue()) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/menu_item_card.fxml"));
                    Parent card = loader.load();
                    MenuItemCardController ctrl = loader.getController();
                    ctrl.setItem(item, this::updateCartUI);
                    itemPane.getChildren().add(card);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            categoryBox.getChildren().addAll(catLabel, itemPane);
            menuContainer.getChildren().add(categoryBox);
        }
    }

    private void updateCartUI() {
        List<OrderItem> cart = Session.getCart();
        List<String> displayList = cart.stream()
                .map(oi -> String.format("%dx %s (%s)", oi.getQuantity(), oi.getItemName(), currencyFormat.format(oi.getUnitPrice())))
                .collect(Collectors.toList());
        
        cartListView.setItems(FXCollections.observableArrayList(displayList));

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
