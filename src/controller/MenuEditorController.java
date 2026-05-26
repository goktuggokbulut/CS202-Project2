package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import model.Coupon;
import model.MenuCategory;
import model.MenuItem;
import model.Restaurant;
import repository.CouponRepository;
import repository.MenuRepository;
import repository.RestaurantRepository;
import utils.Session;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MenuEditorController {

    // ── Restaurant selector ──────────────────────────────────────────
    @FXML private ComboBox<Restaurant> restaurantSelector;

    // ── Menu Items tab ───────────────────────────────────────────────
    @FXML private TableView<MenuItem>                        itemTable;
    @FXML private TableColumn<MenuItem, String>              itemCategoryCol;
    @FXML private TableColumn<MenuItem, String>              itemNameCol;
    @FXML private TableColumn<MenuItem, String>              itemDescCol;
    @FXML private TableColumn<MenuItem, BigDecimal>          itemPriceCol;

    // ── Categories tab ───────────────────────────────────────────────
    @FXML private TableView<MenuCategory>                    categoryTable;
    @FXML private TableColumn<MenuCategory, String>          catNameCol;

    // ── Coupons tab ──────────────────────────────────────────────────
    @FXML private TableView<Coupon>                          couponTable;
    @FXML private TableColumn<Coupon, String>                couponCodeCol;
    @FXML private TableColumn<Coupon, String>                couponTypeCol;
    @FXML private TableColumn<Coupon, BigDecimal>            couponValueCol;
    @FXML private TableColumn<Coupon, LocalDate>             couponFromCol;
    @FXML private TableColumn<Coupon, LocalDate>             couponUntilCol;
    @FXML private TableColumn<Coupon, Boolean>               couponActiveCol;

    private final RestaurantRepository restaurantRepository = new RestaurantRepository();
    private final MenuRepository       menuRepository       = new MenuRepository();
    private final CouponRepository     couponRepository     = new CouponRepository();

    private final ObservableList<MenuItem>     items      = FXCollections.observableArrayList();
    private final ObservableList<MenuCategory> categories = FXCollections.observableArrayList();
    private final ObservableList<Coupon>       coupons    = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // ── Table column bindings ───────────────────────────────────
        itemCategoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        itemNameCol    .setCellValueFactory(new PropertyValueFactory<>("name"));
        itemDescCol    .setCellValueFactory(new PropertyValueFactory<>("description"));
        itemPriceCol   .setCellValueFactory(new PropertyValueFactory<>("price"));
        itemTable.setItems(items);

        catNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryTable.setItems(categories);

        couponCodeCol  .setCellValueFactory(new PropertyValueFactory<>("code"));
        couponTypeCol  .setCellValueFactory(new PropertyValueFactory<>("discountType"));
        couponValueCol .setCellValueFactory(new PropertyValueFactory<>("discountValue"));
        couponFromCol  .setCellValueFactory(new PropertyValueFactory<>("validFrom"));
        couponUntilCol .setCellValueFactory(new PropertyValueFactory<>("validUntil"));
        couponActiveCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        couponTable.setItems(coupons);

        // ── Populate restaurant selector ───────────────────────────
        String managerUsername = Session.getCurrentUser().getUsername();
        List<Restaurant> restaurants = restaurantRepository.getRestaurantsByManager(managerUsername);
        restaurantSelector.setItems(FXCollections.observableArrayList(restaurants));
        restaurantSelector.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> { if (newVal != null) reloadAll(newVal); }
        );
        if (!restaurants.isEmpty()) {
            restaurantSelector.getSelectionModel().selectFirst();
        }
    }

    private void reloadAll(Restaurant r) {
        items     .setAll(menuRepository.getMenuByRestaurant(r.getRestaurantId()));
        categories.setAll(menuRepository.getCategories(r.getRestaurantId()));
        coupons   .setAll(couponRepository.getCouponsByRestaurant(r.getRestaurantId()));
    }

    private Restaurant selectedRestaurant() {
        return restaurantSelector.getSelectionModel().getSelectedItem();
    }

    // ══════════════════════════════════════════════════════════════════
    //  MENU ITEMS
    // ══════════════════════════════════════════════════════════════════

    @FXML
    private void handleAddItem() {
        Restaurant r = selectedRestaurant();
        if (r == null) return;
        List<MenuCategory> cats = menuRepository.getCategories(r.getRestaurantId());
        if (cats.isEmpty()) {
            showError("No categories found. Add a category first.");
            return;
        }
        showItemDialog(null, cats).ifPresent(item -> {
            item.setRestaurantId(r.getRestaurantId());
            if (menuRepository.addMenuItem(item) != null) reloadAll(r);
        });
    }

    @FXML
    private void handleEditItem() {
        MenuItem selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select an item to edit."); return; }
        Restaurant r = selectedRestaurant();
        List<MenuCategory> cats = menuRepository.getCategories(r.getRestaurantId());
        showItemDialog(selected, cats).ifPresent(updated -> {
            if (menuRepository.updateMenuItem(updated)) reloadAll(r);
        });
    }

    @FXML
    private void handleDeleteItem() {
        MenuItem selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select an item to delete."); return; }
        if (confirmDelete("Delete item \"" + selected.getName() + "\"?")) {
            if (menuRepository.deleteMenuItem(selected.getItemId())) reloadAll(selectedRestaurant());
        }
    }

    private Optional<MenuItem> showItemDialog(MenuItem existing, List<MenuCategory> cats) {
        Dialog<MenuItem> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Menu Item" : "Edit Menu Item");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField  = new TextField(existing != null ? existing.getName() : "");
        TextField descField  = new TextField(existing != null ? existing.getDescription() : "");
        TextField priceField = new TextField(existing != null ? existing.getPrice().toPlainString() : "");
        TextField imgField   = new TextField(existing != null ? existing.getImageUrl() : "");
        ComboBox<MenuCategory> catBox = new ComboBox<>(FXCollections.observableArrayList(cats));
        catBox.setConverter(new javafx.util.StringConverter<MenuCategory>() {
            public String toString(MenuCategory c)   { return c == null ? "" : c.getName(); }
            public MenuCategory fromString(String s) { return null; }
        });
        if (existing != null) {
            cats.stream().filter(c -> c.getCategoryId() == existing.getCategoryId())
                .findFirst().ifPresent(catBox.getSelectionModel()::select);
        } else {
            catBox.getSelectionModel().selectFirst();
        }

        grid.addRow(0, new Label("Name:"),        nameField);
        grid.addRow(1, new Label("Description:"), descField);
        grid.addRow(2, new Label("Price:"),        priceField);
        grid.addRow(3, new Label("Image URL:"),    imgField);
        grid.addRow(4, new Label("Category:"),     catBox);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            try {
                MenuItem item = existing != null ? existing : new MenuItem();
                item.setName(nameField.getText().trim());
                item.setDescription(descField.getText().trim());
                item.setPrice(new BigDecimal(priceField.getText().trim()));
                item.setImageUrl(imgField.getText().trim());
                MenuCategory cat = catBox.getSelectionModel().getSelectedItem();
                if (cat != null) {
                    item.setCategoryId(cat.getCategoryId());
                    item.setCategoryName(cat.getName());
                }
                return item;
            } catch (NumberFormatException e) {
                showError("Price must be a valid number.");
                return null;
            }
        });

        return dialog.showAndWait().filter(i -> i != null);
    }

    // ══════════════════════════════════════════════════════════════════
    //  CATEGORIES
    // ══════════════════════════════════════════════════════════════════

    @FXML
    private void handleAddCategory() {
        Restaurant r = selectedRestaurant();
        if (r == null) return;
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Add Category"); dlg.setHeaderText(null);
        dlg.setContentText("Category name:");
        dlg.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                MenuCategory cat = new MenuCategory();
                cat.setName(name.trim());
                cat.setRestaurantId(r.getRestaurantId());
                if (menuRepository.addCategory(cat) != null) reloadAll(r);
            }
        });
    }

    @FXML
    private void handleDeleteCategory() {
        MenuCategory selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select a category to delete."); return; }
        if (confirmDelete("Delete category \"" + selected.getName() + "\"? Items in this category cannot be deleted while items reference it.")) {
            if (menuRepository.deleteCategory(selected.getCategoryId())) reloadAll(selectedRestaurant());
            else showError("Could not delete. Ensure no menu items are in this category.");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  COUPONS
    // ══════════════════════════════════════════════════════════════════

    @FXML
    private void handleAddCoupon() {
        Restaurant r = selectedRestaurant();
        if (r == null) return;
        showCouponDialog(r.getRestaurantId()).ifPresent(coupon -> {
            if (couponRepository.createCoupon(coupon) != null) reloadAll(r);
        });
    }

    @FXML
    private void handleDeactivateCoupon() {
        Coupon selected = couponTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select a coupon to deactivate."); return; }
        if (!selected.isActive()) { showError("Coupon is already inactive."); return; }
        if (couponRepository.deactivateCoupon(selected.getCouponId())) reloadAll(selectedRestaurant());
    }

    private Optional<Coupon> showCouponDialog(int restaurantId) {
        Dialog<Coupon> dialog = new Dialog<>();
        dialog.setTitle("Create Coupon");

        ButtonType saveBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField       codeField   = new TextField();
        ComboBox<String> typeBox    = new ComboBox<>(FXCollections.observableArrayList("percentage", "fixed"));
        typeBox.getSelectionModel().selectFirst();
        TextField       valueField  = new TextField();
        DatePicker      fromPicker  = new DatePicker(LocalDate.now());
        DatePicker      untilPicker = new DatePicker(LocalDate.now().plusMonths(3));

        grid.addRow(0, new Label("Code:"),           codeField);
        grid.addRow(1, new Label("Discount type:"),  typeBox);
        grid.addRow(2, new Label("Discount value:"), valueField);
        grid.addRow(3, new Label("Valid from:"),      fromPicker);
        grid.addRow(4, new Label("Valid until:"),     untilPicker);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            try {
                Coupon c = new Coupon();
                c.setCode(codeField.getText().trim().toUpperCase());
                c.setDiscountType(typeBox.getValue());
                c.setDiscountValue(new BigDecimal(valueField.getText().trim()));
                c.setValidFrom(fromPicker.getValue());
                c.setValidUntil(untilPicker.getValue());
                c.setActive(true);
                c.setRestaurantId(restaurantId);
                return c;
            } catch (NumberFormatException e) {
                showError("Discount value must be a valid number.");
                return null;
            }
        });

        return dialog.showAndWait().filter(c -> c != null);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private boolean confirmDelete(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.show();
    }
}
