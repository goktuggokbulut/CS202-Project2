package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Order;
import model.OrderItem;
import repository.OrderRepository;
import utils.Session;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class OrderRequestsController {

    @FXML private VBox ordersContainer;
    @FXML private Label emptyLabel;

    private final OrderRepository orderRepository = new OrderRepository();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm, MMM dd");

    @FXML
    public void initialize() {
        loadPendingOrders();
    }

    @FXML
    private void loadPendingOrders() {
        ordersContainer.getChildren().clear();
        
        // Mocking restaurant ID 1 for now
        int restaurantId = 1;
        List<Order> pendingOrders = orderRepository.getOrdersByRestaurantAndStatus(restaurantId, "Preparing");

        if (pendingOrders.isEmpty()) {
            emptyLabel.setVisible(true);
            ordersContainer.getChildren().add(emptyLabel);
        } else {
            emptyLabel.setVisible(false);
            for (Order order : pendingOrders) {
                ordersContainer.getChildren().add(createOrderCard(order));
            }
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #d0d7de; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 20;");

        HBox header = new HBox(10);
        VBox titleInfo = new VBox(2);
        Label idLabel = new Label("Order #" + order.getOrderId());
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label customerLabel = new Label("From: " + order.getCustomerId() + " (" + order.getRestaurantName() + ")");
        customerLabel.setStyle("-fx-text-fill: #57606a;");
        titleInfo.getChildren().addAll(idLabel, customerLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label timeLabel = new Label(order.getCreatedAt().format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: #57606a;");
        
        header.getChildren().addAll(titleInfo, spacer, timeLabel);

        VBox itemsBox = new VBox(5);
        List<OrderItem> items = orderRepository.getOrderItems(order.getOrderId());
        for (OrderItem item : items) {
            Label itemLabel = new Label("• " + item.getQuantity() + "x " + item.getItemName());
            itemsBox.getChildren().add(itemLabel);
        }

        HBox footer = new HBox(15);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label totalLabel = new Label("Total: " + currencyFormat.format(order.getTotalAmount()));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #0969da;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);

        Button acceptBtn = new Button("Accept Order");
        acceptBtn.getStyleClass().addAll("btn-primary");
        acceptBtn.setOnAction(e -> handleAccept(order.getOrderId()));

        footer.getChildren().addAll(totalLabel, spacer2, acceptBtn);

        card.getChildren().addAll(header, new Separator(), itemsBox, new Separator(), footer);
        return card;
    }

    private void handleAccept(int orderId) {
        if (orderRepository.acceptOrder(orderId)) {
            loadPendingOrders();
        }
    }
}
