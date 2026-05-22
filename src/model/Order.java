package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the ORDER table.
 * total_amount is stored as deliberate denormalization (see report §5.5).
 */
public class Order {

    private int            orderId;
    private String         customerId;
    private int            restaurantId;
    private Integer        couponId;        // nullable
    private String         status;          // Preparing | Sent | Accepted
    private LocalDateTime  createdAt;
    private BigDecimal     totalAmount;
    private BigDecimal     discountApplied;

    // Populated for display convenience
    private List<OrderItem>          items;
    private List<OrderStatusHistory> statusHistory;
    private String                   restaurantName;

    public Order() {}

    public int           getOrderId()                       { return orderId; }
    public void          setOrderId(int v)                  { this.orderId = v; }
    public String        getCustomerId()                    { return customerId; }
    public void          setCustomerId(String v)            { this.customerId = v; }
    public int           getRestaurantId()                  { return restaurantId; }
    public void          setRestaurantId(int v)             { this.restaurantId = v; }
    public Integer       getCouponId()                      { return couponId; }
    public void          setCouponId(Integer v)             { this.couponId = v; }
    public String        getStatus()                        { return status; }
    public void          setStatus(String v)                { this.status = v; }
    public LocalDateTime getCreatedAt()                     { return createdAt; }
    public void          setCreatedAt(LocalDateTime v)      { this.createdAt = v; }
    public BigDecimal    getTotalAmount()                   { return totalAmount; }
    public void          setTotalAmount(BigDecimal v)       { this.totalAmount = v; }
    public BigDecimal    getDiscountApplied()               { return discountApplied; }
    public void          setDiscountApplied(BigDecimal v)   { this.discountApplied = v; }
    public List<OrderItem> getItems()                       { return items; }
    public void          setItems(List<OrderItem> v)        { this.items = v; }
    public List<OrderStatusHistory> getStatusHistory()      { return statusHistory; }
    public void          setStatusHistory(List<OrderStatusHistory> v){ this.statusHistory = v; }
    public String        getRestaurantName()                { return restaurantName; }
    public void          setRestaurantName(String v)        { this.restaurantName = v; }
}