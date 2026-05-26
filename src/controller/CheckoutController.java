package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Coupon;
import model.Order;
import model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import repository.CouponRepository;
import service.OrderService;
import utils.Session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class CheckoutController {

    @FXML private ListView<String> summaryListView;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;
    @FXML private TextField couponField;
    @FXML private Label couponMessageLabel;
    @FXML private Label placeOrderMessageLabel;

    @Autowired private OrderService orderService;
    @Autowired private CouponRepository couponRepository;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));

    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private Coupon appliedCoupon = null;

    @FXML
    public void initialize() {
        loadSummary();
    }

    private void loadSummary() {
        List<OrderItem> cart = Session.getCart();
        List<String> items = cart.stream()
                .map(oi -> String.format("%dx %s - %s", oi.getQuantity(), oi.getItemName(), currencyFormat.format(oi.getUnitPrice().multiply(new BigDecimal(oi.getQuantity())))))
                .collect(Collectors.toList());
        summaryListView.setItems(FXCollections.observableArrayList(items));

        subtotal = cart.stream()
                .map(oi -> oi.getUnitPrice().multiply(new BigDecimal(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        updateTotals();
    }

    private void updateTotals() {
        subtotalLabel.setText(currencyFormat.format(subtotal));
        discountLabel.setText("-" + currencyFormat.format(discount));
        totalLabel.setText(currencyFormat.format(subtotal.subtract(discount).max(BigDecimal.ZERO)));
    }

    @FXML
    private void handleApplyCoupon() {
        String code = couponField.getText().trim();
        if (code.isEmpty()) return;

        int restaurantId = Session.getSelectedRestaurant().getRestaurantId();
        Coupon coupon = couponRepository.getValidCouponByCode(code, restaurantId);

        if (coupon != null) {
            appliedCoupon = coupon;
            if ("percentage".equalsIgnoreCase(coupon.getDiscountType())) {
                discount = subtotal.multiply(coupon.getDiscountValue().divide(new BigDecimal(100), 4, RoundingMode.HALF_UP));
            } else {
                discount = coupon.getDiscountValue();
            }
            couponMessageLabel.setText("Coupon applied: " + code);
            couponMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            updateTotals();
        } else {
            couponMessageLabel.setText("Invalid or expired coupon.");
            couponMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            discount = BigDecimal.ZERO;
            appliedCoupon = null;
            updateTotals();
        }
    }

    @FXML
    private void handlePlaceOrder() {
        try {
            if (Session.getCart().isEmpty()) {
                placeOrderMessageLabel.setText("Your cart is empty.");
                placeOrderMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }
            Order order = new Order();
            order.setCustomerId(Session.getCurrentUser().getUsername());
            order.setRestaurantId(Session.getSelectedRestaurant().getRestaurantId());
            if (appliedCoupon != null) {
                order.setCouponId(appliedCoupon.getCouponId());
            }

            if (orderService.placeOrder(order, Session.getCart())) {
                placeOrderMessageLabel.setText("Order placed successfully! Redirecting...");
                placeOrderMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                Session.clearCart();
                MainDashboardController.getInstance().loadViewByPath("orders");
            } else {
                placeOrderMessageLabel.setText("Failed to place order. Check the console for details.");
                placeOrderMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            placeOrderMessageLabel.setText("Error: " + e.getMessage());
            placeOrderMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleBack() {
        MainDashboardController.getInstance().loadViewByPath("menu_view");
    }
}
