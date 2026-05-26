package utils;

import model.User;

public class Session {
    private static User currentUser;

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    private static model.Restaurant selectedRestaurant;
    private static java.util.List<model.OrderItem> cart = new java.util.ArrayList<>();

    public static void setSelectedRestaurant(model.Restaurant r) {
        selectedRestaurant = r;
    }

    public static model.Restaurant getSelectedRestaurant() {
        return selectedRestaurant;
    }

    public static java.util.List<model.OrderItem> getCart() {
        return cart;
    }

    public static void addToCart(model.MenuItem item) {
        for (model.OrderItem oi : cart) {
            if (oi.getItemId() == item.getItemId()) {
                oi.setQuantity(oi.getQuantity() + 1);
                return;
            }
        }
        model.OrderItem newItem = new model.OrderItem();
        newItem.setItemId(item.getItemId());
        newItem.setItemName(item.getName());
        newItem.setUnitPrice(item.getPrice());
        newItem.setQuantity(1);
        cart.add(newItem);
    }

    public static void removeFromCart(int itemId) {
        cart.removeIf(oi -> oi.getItemId() == itemId);
    }

    public static void decreaseFromCart(int itemId) {
        for (model.OrderItem oi : cart) {
            if (oi.getItemId() == itemId) {
                if (oi.getQuantity() <= 1) cart.remove(oi);
                else oi.setQuantity(oi.getQuantity() - 1);
                return;
            }
        }
    }

    public static void clearCart() {
        cart.clear();
    }
}
