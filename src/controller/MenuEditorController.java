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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import model.Coupon;
import model.MenuCategory;
import model.MenuItem;
import model.Restaurant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import repository.CouponRepository;
import repository.MenuRepository;
import repository.RestaurantRepository;
import utils.Session;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
public class MenuEditorController {

    @FXML private ComboBox<Restaurant> restaurantSelector;

    @FXML private TableView<MenuItem>                        itemTable;
    @FXML private TableColumn<MenuItem, String>              itemCategoryCol;
    @FXML private TableColumn<MenuItem, String>              itemNameCol;
    @FXML private TableColumn<MenuItem, String>              itemDescCol;
    @FXML private TableColumn<MenuItem, BigDecimal>          itemPriceCol;

    @FXML private TableView<MenuCategory>                    categoryTable;
    @FXML private TableColumn<MenuCategory, String>          catNameCol;

    @FXML private TableView<Coupon>                          couponTable;
    @FXML private TableColumn<Coupon, String>                couponCodeCol;
    @FXML private TableColumn<Coupon, String>                couponTypeCol;
    @FXML private TableColumn<Coupon, BigDecimal>            couponValueCol;
    @FXML private TableColumn<Coupon, LocalDate>             couponFromCol;
    @FXML private TableColumn<Coupon, LocalDate>             couponUntilCol;
    @FXML private TableColumn<Coupon, Boolean>               couponActiveCol;

    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private MenuRepository       menuRepository;
    @Autowired private CouponRepository     couponRepository;

    private final ObservableList<MenuItem>     items      = FXCollections.observableArrayList();
    private final ObservableList<MenuCategory> categories = FXCollections.observableArrayList();
    private final ObservableList<Coupon>       coupons    = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
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

        final String[] selectedImagePath = { existing != null && existing.getImageUrl() != null ? existing.getImageUrl() : "" };
        boolean hasExistingImage = !selectedImagePath[0].isEmpty();

        CheckBox imgCheckBox = new CheckBox("Custom image");
        imgCheckBox.setSelected(hasExistingImage);

        ImageView preview = new ImageView();
        preview.setFitWidth(120); preview.setFitHeight(70); preview.setPreserveRatio(true);
        loadPreview(preview, selectedImagePath[0]);

        Label imgNameLabel = new Label(hasExistingImage ? Paths.get(selectedImagePath[0]).getFileName().toString() : "No image selected");
        imgNameLabel.setStyle("-fx-text-fill: #57606a; -fx-font-size: 11px;");

        Button browseBtn = new Button("Browse…");
        browseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Item Image");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"));
            File file = fc.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                String storedPath = copyImageToResources(file);
                if (storedPath != null) {
                    selectedImagePath[0] = storedPath;
                    imgNameLabel.setText(file.getName());
                    loadPreview(preview, storedPath);
                }
            }
        });

        VBox imgPickerBox = new VBox(6, preview, imgNameLabel, browseBtn);
        imgPickerBox.setVisible(hasExistingImage);
        imgPickerBox.setManaged(hasExistingImage);

        imgCheckBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            imgPickerBox.setVisible(isSelected);
            imgPickerBox.setManaged(isSelected);
            if (!isSelected) {
                selectedImagePath[0] = "";
                preview.setImage(null);
                imgNameLabel.setText("No image selected");
            }
        });

        VBox imgBox = new VBox(6, imgCheckBox, imgPickerBox);

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
        grid.addRow(3, new Label("Image:"),        imgBox);
        grid.addRow(4, new Label("Category:"),     catBox);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            try {
                MenuItem item = existing != null ? existing : new MenuItem();
                item.setName(nameField.getText().trim());
                item.setDescription(descField.getText().trim());
                item.setPrice(new BigDecimal(priceField.getText().trim()));
                item.setImageUrl(selectedImagePath[0]);
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

    private String copyImageToResources(File sourceFile) {
        try {
            URL classLocation = getClass().getProtectionDomain().getCodeSource().getLocation();
            Path targetClasses = Paths.get(classLocation.toURI());
            Path projectRoot   = targetClasses.getParent().getParent();

            Path runtimeDir  = targetClasses.resolve("img/items");
            Path resourceDir = projectRoot.resolve("resources/img/items");
            Files.createDirectories(runtimeDir);
            Files.createDirectories(resourceDir);

            String filename = sourceFile.getName();
            Files.copy(sourceFile.toPath(), runtimeDir .resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(sourceFile.toPath(), resourceDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

            return "img/items/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not copy image: " + e.getMessage());
            return null;
        }
    }

    private void loadPreview(ImageView view, String path) {
        if (path == null || path.isBlank()) { view.setImage(null); return; }
        try {
            URL resource = getClass().getResource("/" + path);
            String url = resource != null ? resource.toExternalForm() : path;
            view.setImage(new Image(url, 120, 70, true, true, true));
        } catch (Exception e) {
            view.setImage(null);
        }
    }

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
