package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.RestaurantStatistics;
import repository.RestaurantRepository;
import utils.Session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class StatisticsController {

    @FXML private Label totalRevenueLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label avgOrderLabel;
    @FXML private Label totalDiscountLabel;

    @FXML private TableView<RestaurantStatistics.ItemSale> itemSalesTable;
    @FXML private TableColumn<RestaurantStatistics.ItemSale, String> itemNameCol;
    @FXML private TableColumn<RestaurantStatistics.ItemSale, Integer> unitsSoldCol;
    @FXML private TableColumn<RestaurantStatistics.ItemSale, BigDecimal> revenueCol;

    @FXML private Label topCustomerLabel;
    @FXML private Label highestOrderLabel;
    @FXML private Label popularItemLabel;
    @FXML private Label topCategoryLabel;

    private final RestaurantRepository restaurantRepository = new RestaurantRepository();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    public void initialize() {
        itemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        unitsSoldCol.setCellValueFactory(new PropertyValueFactory<>("unitsSold"));
        revenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        loadStatistics();
    }

    private void loadStatistics() {
        // In a real flow, we'd get the restaurantId from the logged-in manager.
        // For now, let's assume we find the first restaurant managed by this user.
        // (Mocking ID 1 for demonstration if not found)
        int restaurantId = 1; 

        RestaurantStatistics stats = new RestaurantStatistics();
        
        // Fetch all stats using the repository methods we implemented
        restaurantRepository.fillGeneralStats(restaurantId, stats);
        restaurantRepository.fillTopStats(restaurantId, stats);
        restaurantRepository.fillAggregatedStats(restaurantId, stats);
        stats.setItemSales(restaurantRepository.getMonthlyItemSales(restaurantId));

        // Update UI
        totalRevenueLabel.setText(currencyFormat.format(stats.getTotalRevenue()));
        totalOrdersLabel.setText(String.valueOf(stats.getTotalOrders()));
        
        if (stats.getTotalOrders() > 0) {
            BigDecimal avg = stats.getTotalRevenue().divide(new BigDecimal(stats.getTotalOrders()), 2, RoundingMode.HALF_UP);
            avgOrderLabel.setText(currencyFormat.format(avg));
        }

        totalDiscountLabel.setText("-" + currencyFormat.format(stats.getTotalCouponDiscount()));

        itemSalesTable.setItems(FXCollections.observableArrayList(stats.getItemSales()));

        if (stats.getTopCustomerByCount() != null) {
            topCustomerLabel.setText(stats.getTopCustomerByCount().customerId + " (" + stats.getTopCustomerByCount().orderCount + " orders)");
        }

        if (stats.getTopOrder() != null) {
            highestOrderLabel.setText(currencyFormat.format(stats.getTopOrder().amount) + " by " + stats.getTopOrder().customerId);
        }

        popularItemLabel.setText(stats.getMostOrderedItem() != null ? stats.getMostOrderedItem() : "N/A");
        topCategoryLabel.setText(stats.getTopRevenueCategory() != null ? stats.getTopRevenueCategory() : "N/A");
    }
}
