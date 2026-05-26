package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Restaurant;
import repository.RestaurantRepository;
import utils.Session;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RestaurantSettingsController {

    @FXML private ComboBox<Restaurant> restaurantSelector;
    @FXML private VBox                 formBox;
    @FXML private Label                statusLabel;
    @FXML private Label                formTitleLabel;

    @FXML private TextField nameField;
    @FXML private TextField cuisineField;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private TextField keywordsField;   // comma-separated

    @FXML private Button saveButton;

    private final RestaurantRepository restaurantRepository = new RestaurantRepository();
    private Restaurant editingRestaurant = null;   // null → create mode

    @FXML
    public void initialize() {
        String managerUsername = Session.getCurrentUser().getUsername();
        List<Restaurant> restaurants = restaurantRepository.getRestaurantsByManager(managerUsername);

        if (restaurants.isEmpty()) {
            // Create mode
            enterCreateMode(managerUsername);
        } else {
            // Edit mode — populate selector
            restaurantSelector.setItems(FXCollections.observableArrayList(restaurants));
            restaurantSelector.setVisible(true);
            restaurantSelector.setManaged(true);
            restaurantSelector.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> { if (newVal != null) enterEditMode(newVal); }
            );
            restaurantSelector.getSelectionModel().selectFirst();
        }
    }

    // ── Mode helpers ─────────────────────────────────────────────────

    private void enterCreateMode(String managerUsername) {
        editingRestaurant = null;
        formTitleLabel.setText("Create Your Restaurant");
        saveButton.setText("Create Restaurant");
        clearForm();
        // Pre-fill city from the manager's own profile
        cityField.setText(Session.getCurrentUser().getCity());
        statusLabel.setText("You don't have a restaurant yet. Fill in the details below to create one.");
        statusLabel.setStyle("-fx-text-fill: #9a6700;");
        statusLabel.setVisible(true);
    }

    private void enterEditMode(Restaurant r) {
        editingRestaurant = r;
        formTitleLabel.setText("Edit Restaurant");
        saveButton.setText("Save Changes");
        nameField.setText(r.getName());
        cuisineField.setText(r.getCuisineType());
        addressField.setText(r.getAddress());
        cityField.setText(r.getCity());
        // Load existing keywords from DB
        List<String> kws = restaurantRepository.getKeywords(r.getRestaurantId());
        keywordsField.setText(String.join(", ", kws));
        statusLabel.setVisible(false);
    }

    // ── Save handler ─────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        String name    = nameField.getText().trim();
        String cuisine = cuisineField.getText().trim();
        String address = addressField.getText().trim();
        String city    = cityField.getText().trim();
        List<String> keywords = Arrays.stream(keywordsField.getText().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (name.isEmpty() || cuisine.isEmpty() || address.isEmpty() || city.isEmpty()) {
            showStatus("All fields except keywords are required.", "-fx-text-fill: #cf222e;");
            return;
        }

        if (editingRestaurant == null) {
            // CREATE
            Restaurant r = new Restaurant();
            r.setName(name);
            r.setCuisineType(cuisine);
            r.setAddress(address);
            r.setCity(city);
            r.setManagerId(Session.getCurrentUser().getUsername());
            r.setKeywords(keywords);
            Restaurant created = restaurantRepository.createRestaurant(r);
            if (created != null) {
                editingRestaurant = created;
                restaurantSelector.getItems().add(created);
                restaurantSelector.setVisible(true);
                restaurantSelector.setManaged(true);
                restaurantSelector.getSelectionModel().select(created);
                formTitleLabel.setText("Edit Restaurant");
                saveButton.setText("Save Changes");
                showStatus("Restaurant created successfully!", "-fx-text-fill: #1a7f37;");
            } else {
                showStatus("Failed to create restaurant.", "-fx-text-fill: #cf222e;");
            }
        } else {
            // UPDATE
            editingRestaurant.setName(name);
            editingRestaurant.setCuisineType(cuisine);
            editingRestaurant.setAddress(address);
            editingRestaurant.setCity(city);
            editingRestaurant.setKeywords(keywords);
            if (restaurantRepository.updateRestaurant(editingRestaurant)) {
                // Refresh selector display
                int idx = restaurantSelector.getSelectionModel().getSelectedIndex();
                restaurantSelector.getItems().set(idx, editingRestaurant);
                restaurantSelector.getSelectionModel().select(idx);
                showStatus("Restaurant updated successfully!", "-fx-text-fill: #1a7f37;");
            } else {
                showStatus("Failed to update restaurant.", "-fx-text-fill: #cf222e;");
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void clearForm() {
        nameField.clear();
        cuisineField.clear();
        addressField.clear();
        keywordsField.clear();
    }

    private void showStatus(String message, String style) {
        statusLabel.setText(message);
        statusLabel.setStyle(style);
        statusLabel.setVisible(true);
    }
}
