package model;

import java.math.BigDecimal;

/**
 * Represents the MENU_ITEM table.
 * Note: restaurantId is NOT stored on this table – it is derived
 * via MenuItem → MenuCategory → Restaurant (preserves 3NF).
 */
public class MenuItem {

    private int        itemId;
    private String     name;
    private String     description;
    private BigDecimal price;
    private String     imageUrl;
    private int        categoryId;

    // Populated for display convenience (not stored)
    private String categoryName;
    private int    restaurantId;

    public MenuItem() {}

    public int        getItemId()               { return itemId; }
    public void       setItemId(int v)           { this.itemId = v; }
    public String     getName()                  { return name; }
    public void       setName(String v)          { this.name = v; }
    public String     getDescription()           { return description; }
    public void       setDescription(String v)   { this.description = v; }
    public BigDecimal getPrice()                 { return price; }
    public void       setPrice(BigDecimal v)     { this.price = v; }
    public String     getImageUrl()              { return imageUrl; }
    public void       setImageUrl(String v)      { this.imageUrl = v; }
    public int        getCategoryId()            { return categoryId; }
    public void       setCategoryId(int v)       { this.categoryId = v; }
    public String     getCategoryName()          { return categoryName; }
    public void       setCategoryName(String v)  { this.categoryName = v; }
    public int        getRestaurantId()          { return restaurantId; }
    public void       setRestaurantId(int v)     { this.restaurantId = v; }
}