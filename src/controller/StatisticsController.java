package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import model.OrderItem;
import model.Restaurant;
import model.RestaurantStatistics;
import repository.OrderRepository;
import repository.RestaurantRepository;
import utils.Session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StatisticsController {

    @FXML private ComboBox<Restaurant> restaurantSelector;

    @FXML private Label totalRevenueLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label avgOrderLabel;
    @FXML private Label totalDiscountLabel;

    @FXML private TableView<RestaurantStatistics.ItemSale> itemSalesTable;
    @FXML private TableColumn<RestaurantStatistics.ItemSale, String>     itemNameCol;
    @FXML private TableColumn<RestaurantStatistics.ItemSale, Integer>    unitsSoldCol;
    @FXML private TableColumn<RestaurantStatistics.ItemSale, BigDecimal> revenueCol;

    @FXML private Label topCustomerLabel;
    @FXML private Label highestOrderLabel;
    @FXML private VBox  topOrderItemsBox;
    @FXML private Label popularItemLabel;
    @FXML private Label topCategoryLabel;

    private final RestaurantRepository restaurantRepository = new RestaurantRepository();
    private final OrderRepository      orderRepository      = new OrderRepository();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    @FXML
    public void initialize() {
        itemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        unitsSoldCol.setCellValueFactory(new PropertyValueFactory<>("unitsSold"));
        revenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        String managerUsername = Session.getCurrentUser().getUsername();
        List<Restaurant> restaurants = restaurantRepository.getRestaurantsByManager(managerUsername);

        restaurantSelector.setItems(FXCollections.observableArrayList(restaurants));
        restaurantSelector.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> { if (newVal != null) loadStatistics(newVal.getRestaurantId()); }
        );

        if (!restaurants.isEmpty()) {
            restaurantSelector.getSelectionModel().selectFirst();
        }
    }

    private void loadStatistics(int restaurantId) {
        RestaurantStatistics stats = new RestaurantStatistics();

        restaurantRepository.fillGeneralStats(restaurantId, stats);
        restaurantRepository.fillTopStats(restaurantId, stats);
        restaurantRepository.fillAggregatedStats(restaurantId, stats);
        stats.setItemSales(restaurantRepository.getMonthlyItemSales(restaurantId));

        BigDecimal revenue  = stats.getTotalRevenue()       != null ? stats.getTotalRevenue()       : BigDecimal.ZERO;
        BigDecimal discount = stats.getTotalCouponDiscount() != null ? stats.getTotalCouponDiscount() : BigDecimal.ZERO;

        totalRevenueLabel.setText(currencyFormat.format(revenue));
        totalOrdersLabel.setText(String.valueOf(stats.getTotalOrders()));

        if (stats.getTotalOrders() > 0) {
            BigDecimal avg = revenue.divide(new BigDecimal(stats.getTotalOrders()), 2, RoundingMode.HALF_UP);
            avgOrderLabel.setText(currencyFormat.format(avg));
        } else {
            avgOrderLabel.setText(currencyFormat.format(BigDecimal.ZERO));
        }

        totalDiscountLabel.setText("-" + currencyFormat.format(discount));

        itemSalesTable.setItems(FXCollections.observableArrayList(stats.getItemSales()));

        topCustomerLabel.setText(stats.getTopCustomerByCount() != null
                ? stats.getTopCustomerByCount().customerId + " (" + stats.getTopCustomerByCount().orderCount + " orders)"
                : "N/A");

        topOrderItemsBox.getChildren().clear();
        if (stats.getTopOrder() != null) {
            highestOrderLabel.setText(currencyFormat.format(stats.getTopOrder().amount)
                    + " by " + stats.getTopOrder().customerId
                    + " (Order #" + stats.getTopOrder().orderId + ")");
            List<OrderItem> topItems = orderRepository.getOrderItems(stats.getTopOrder().orderId);
            for (OrderItem oi : topItems) {
                Label itemLabel = new Label("• " + oi.getQuantity() + "x " + oi.getItemName()
                        + " — " + currencyFormat.format(oi.getUnitPrice().multiply(new BigDecimal(oi.getQuantity()))));
                itemLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #57606a;");
                topOrderItemsBox.getChildren().add(itemLabel);
            }
        } else {
            highestOrderLabel.setText("N/A");
        }

        popularItemLabel.setText(stats.getMostOrderedItem() != null ? stats.getMostOrderedItem() : "N/A");
        topCategoryLabel.setText(stats.getTopRevenueCategory() != null ? stats.getTopRevenueCategory() : "N/A");
    }
}
