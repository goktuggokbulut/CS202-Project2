package model;

/** Represents the MENU_CATEGORY table. */
public class MenuCategory {

    private int    categoryId;
    private String name;
    private int    restaurantId;

    public MenuCategory() {}

    public int    getCategoryId()             { return categoryId; }
    public void   setCategoryId(int v)        { this.categoryId = v; }
    public String getName()                   { return name; }
    public void   setName(String v)           { this.name = v; }
    public int    getRestaurantId()           { return restaurantId; }
    public void   setRestaurantId(int v)      { this.restaurantId = v; }
}