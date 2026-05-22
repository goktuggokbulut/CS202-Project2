package model;

import java.util.List;

/**
 * Represents the RESTAURANT table.
 * avg_rating and ratingCount are derived at query time (never stored).
 */
public class Restaurant {

    private int    restaurantId;
    private String name;
    private String cuisineType;
    private String address;
    private String city;
    private String managerId;

    // Derived / search-result fields (not stored as columns)
    private double  avgRating;
    private int     ratingCount;
    private String  label;          // "New" when ratingCount < 10
    private List<String> keywords;

    public Restaurant() {}

    public int    getRestaurantId()               { return restaurantId; }
    public void   setRestaurantId(int v)          { this.restaurantId = v; }
    public String getName()                       { return name; }
    public void   setName(String v)               { this.name = v; }
    public String getCuisineType()                { return cuisineType; }
    public void   setCuisineType(String v)        { this.cuisineType = v; }
    public String getAddress()                    { return address; }
    public void   setAddress(String v)            { this.address = v; }
    public String getCity()                       { return city; }
    public void   setCity(String v)               { this.city = v; }
    public String getManagerId()                  { return managerId; }
    public void   setManagerId(String v)          { this.managerId = v; }
    public double getAvgRating()                  { return avgRating; }
    public void   setAvgRating(double v)          { this.avgRating = v; }
    public int    getRatingCount()                { return ratingCount; }
    public void   setRatingCount(int v)           { this.ratingCount = v; }
    public String getLabel()                      { return label; }
    public void   setLabel(String v)              { this.label = v; }
    public List<String> getKeywords()             { return keywords; }
    public void   setKeywords(List<String> v)     { this.keywords = v; }
}
