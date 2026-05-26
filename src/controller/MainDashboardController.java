package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.User;
import utils.SceneManager;
import utils.Session;

public class MainDashboardController {

    @FXML private VBox navLinks;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private StackPane contentArea;

    private static MainDashboardController instance;

    @FXML
    public void initialize() {
        instance = this;
        User user = Session.getCurrentUser();
        if (user != null) {
            usernameLabel.setText(user.getUsername());
            roleLabel.setText(user.getUserType());
            setupSidebar(user.getUserType());
        }
    }

    public static MainDashboardController getInstance() {
        return instance;
    }

    public void loadViewByPath(String viewId) {
        loadView(viewId);
    }

    private void setupSidebar(String role) {
        navLinks.getChildren().clear();
        
        if ("Customer".equalsIgnoreCase(role)) {
            addButton("Browse Restaurants", "restaurant_search");
            addButton("My Orders", "orders");
            addButton("Profile", "profile");
        } else {
            addButton("Dashboard Overview", "manager_overview");
            addButton("My Restaurant", "restaurant_settings");
            addButton("Manage Menu", "menu_editor");
            addButton("Order Requests", "order_requests");
            addButton("Monthly Statistics", "statistics");
        }
    }

    private void addButton(String text, String viewId) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("flat"); // AtlantaFX flat style
        btn.setOnAction(e -> loadView(viewId));
        navLinks.getChildren().add(btn);
    }

    private void loadView(String viewId) {
        try {
            String fxmlPath = "/view/" + viewId + ".fxml";
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("CRITICAL: Cannot find resource at " + fxmlPath);
                throw new java.io.IOException("Resource not found");
            }
            javafx.scene.Parent view = javafx.fxml.FXMLLoader.load(resource);
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null) cause = cause.getCause();
            System.err.println("Failed to load view '" + viewId + "': " + cause);
            e.printStackTrace();
            Label errorLabel = new Label("Root cause: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
            errorLabel.setWrapText(true);
            contentArea.getChildren().setAll(errorLabel);
        }
    }

    @FXML
    private void handleLogout() {
        Session.logout();
        SceneManager.switchScene("/view/login.fxml", "Login");
    }
}
