package controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Order;
import model.OrderStatusHistory;
import model.Rating;
import repository.OrderRepository;
import service.RatingService;
import utils.Session;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class OrdersController {

    @FXML private VBox ordersContainer;
    @FXML private Label emptyLabel;

    private final OrderRepository orderRepository = new OrderRepository();
    private final RatingService ratingService = new RatingService();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm, MMM dd");

    @FXML
    public void initialize() {
        loadOrders();
    }

    @FXML
    private void loadOrders() {
        ordersContainer.getChildren().clear();
        String customerId = Session.getCurrentUser().getUsername();
        List<Order> history = orderRepository.getOrdersByCustomer(customerId);

        if (history.isEmpty()) {
            emptyLabel.setVisible(true);
            ordersContainer.getChildren().add(emptyLabel);
        } else {
            emptyLabel.setVisible(false);
            for (Order order : history) {
                ordersContainer.getChildren().add(createOrderCard(order));
            }
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #d0d7de; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 20;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleInfo = new VBox(2);
        Label restLabel = new Label(order.getRestaurantName());
        restLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label dateLabel = new Label(order.getCreatedAt().format(timeFormatter));
        dateLabel.setStyle("-fx-text-fill: #57606a;");
        titleInfo.getChildren().addAll(restLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(order.getStatus().toUpperCase());
        String statusStyle = "-fx-font-weight: bold; -fx-padding: 2 10; -fx-background-radius: 12; -fx-font-size: 11px;";
        if (order.getStatus().equalsIgnoreCase("Accepted")) {
            statusStyle += "-fx-background-color: #dafbe1; -fx-text-fill: #1a7f37;";
        } else {
            statusStyle += "-fx-background-color: #fff8c5; -fx-text-fill: #9a6700;";
        }
        statusLabel.setStyle(statusStyle);
        
        header.getChildren().addAll(titleInfo, spacer, statusLabel);

        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label totalLabel = new Label("Total: " + currencyFormat.format(order.getTotalAmount()));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0969da;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button historyBtn = new Button("View Status History");
        historyBtn.getStyleClass().add("btn-sm");
        historyBtn.setOnAction(e -> showHistory(order.getOrderId()));

        Button rateBtn = new Button("Rate Restaurant");
        rateBtn.getStyleClass().addAll("btn-sm", "btn-success");
        rateBtn.setVisible(order.getStatus().equalsIgnoreCase("Accepted"));
        rateBtn.setOnAction(e -> showRatingDialog(order));

        footer.getChildren().addAll(totalLabel, spacer2, historyBtn, rateBtn);

        card.getChildren().addAll(header, new Separator(), footer);
        return card;
    }

    private void showHistory(int orderId) {
        List<OrderStatusHistory> history = orderRepository.getStatusHistory(orderId);
        StringBuilder sb = new StringBuilder("Order Timeline:\n\n");
        for (OrderStatusHistory h : history) {
            sb.append(String.format("[%s] %s\n", h.getTimeStamp().format(timeFormatter), h.getStatus()));
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Status History");
        alert.setHeaderText("Order #" + orderId);
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    private void showRatingDialog(Order order) {
        // Simple custom dialog for rating
        Dialog<Rating> dialog = new Dialog<>();
        dialog.setTitle("Rate your experience");
        dialog.setHeaderText("Rating for " + order.getRestaurantName());

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label scoreLabel = new Label("Score (1-5):");
        Slider scoreSlider = new Slider(1, 5, 5);
        scoreSlider.setShowTickLabels(true);
        scoreSlider.setShowTickMarks(true);
        scoreSlider.setMajorTickUnit(1);
        scoreSlider.setMinorTickCount(0);
        scoreSlider.setSnapToTicks(true);

        Label commentLabel = new Label("Comment:");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("How was your food?");
        commentArea.setPrefRowCount(3);

        content.getChildren().addAll(scoreLabel, scoreSlider, commentLabel, commentArea);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                Rating r = new Rating();
                r.setOrderId(order.getOrderId());
                r.setCustomerId(Session.getCurrentUser().getUsername());
                r.setScore((int) scoreSlider.getValue());
                r.setComment(commentArea.getText());
                return r;
            }
            return null;
        });

        Optional<Rating> result = dialog.showAndWait();
        result.ifPresent(rating -> {
            if (ratingService.submitRating(rating)) {
                Alert success = new Alert(Alert.AlertType.INFORMATION, "Thank you for your feedback!");
                success.show();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR, "Could not submit rating. Ensure it's within 24 hours of order acceptance.");
                error.show();
            }
        });
    }
}
