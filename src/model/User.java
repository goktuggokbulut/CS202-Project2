package model;
/**
 * Represents the USER supertype table.
 * Subtypes Customer and RestaurantManager extend this class.
 */
public class User {

    private String username;
    private String password;
    private String email;
    private String city;
    private String userType;   // "customer" | "manager"

    public User() {}

    public User(String username, String password, String email,
                String city, String userType) {
        this.username = username;
        this.password = password;
        this.email    = email;
        this.city     = city;
        this.userType = userType;
    }

    public String getUsername()            { return username; }
    public void   setUsername(String v)    { this.username = v; }
    public String getPassword()            { return password; }
    public void   setPassword(String v)    { this.password = v; }
    public String getEmail()               { return email; }
    public void   setEmail(String v)       { this.email = v; }
    public String getCity()                { return city; }
    public void   setCity(String v)        { this.city = v; }
    public String getUserType()            { return userType; }
    public void   setUserType(String v)    { this.userType = v; }
}
