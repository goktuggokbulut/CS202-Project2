package model;

import java.math.BigDecimal;

/**
 * Represents the ORDER_ITEM table.
 * This is the translation of the M:N CONTAINS relationship.
 * unit_price is a snapshot of the price at order time.
 */
public class OrderItem {

    private int        orderId;
    private int        itemId;
    private int        quantity;
    private BigDecimal unitPrice;

    // Display convenience fields
    private String itemName;
    private String imageUrl;

    public OrderItem() {}

    public int        getOrderId()              { return orderId; }
    public void       setOrderId(int v)          { this.orderId = v; }
    public int        getItemId()                { return itemId; }
    public void       setItemId(int v)           { this.itemId = v; }
    public int        getQuantity()              { return quantity; }
    public void       setQuantity(int v)         { this.quantity = v; }
    public BigDecimal getUnitPrice()             { return unitPrice; }
    public void       setUnitPrice(BigDecimal v) { this.unitPrice = v; }
    public String     getItemName()              { return itemName; }
    public void       setItemName(String v)      { this.itemName = v; }
    public String     getImageUrl()              { return imageUrl; }
    public void       setImageUrl(String v)      { this.imageUrl = v; }

    /** Converts this cart entry back to a MenuItem so Session.addToCart() can increment it. */
    public MenuItem toMenuItem() {
        MenuItem m = new MenuItem();
        m.setItemId(itemId);
        m.setName(itemName);
        m.setPrice(unitPrice);
        return m;
    }
}