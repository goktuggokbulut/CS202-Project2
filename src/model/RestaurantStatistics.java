package model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object for Restaurant Monthly Statistics.
 */
public class RestaurantStatistics {
    private int totalOrders;
    private BigDecimal totalRevenue;
    private List<ItemSale> itemSales;
    private TopCustomer topCustomerByCount;
    private TopOrder topOrder;
    private String mostOrderedItem;
    private String topRevenueCategory;
    private BigDecimal totalCouponDiscount;

    // Inner classes for structured data
    public static class ItemSale {
        public int itemId;
        public String itemName;
        public int unitsSold;
        public BigDecimal revenue;
    }

    public static class TopCustomer {
        public String customerId;
        public int orderCount;
    }

    public static class TopOrder {
        public int orderId;
        public String customerId;
        public BigDecimal amount;
        public java.time.LocalDateTime timestamp;
    }

    // Getters and Setters
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public List<ItemSale> getItemSales() { return itemSales; }
    public void setItemSales(List<ItemSale> itemSales) { this.itemSales = itemSales; }

    public TopCustomer getTopCustomerByCount() { return topCustomerByCount; }
    public void setTopCustomerByCount(TopCustomer topCustomerByCount) { this.topCustomerByCount = topCustomerByCount; }

    public TopOrder getTopOrder() { return topOrder; }
    public void setTopOrder(TopOrder topOrder) { this.topOrder = topOrder; }

    public String getMostOrderedItem() { return mostOrderedItem; }
    public void setMostOrderedItem(String mostOrderedItem) { this.mostOrderedItem = mostOrderedItem; }

    public String getTopRevenueCategory() { return topRevenueCategory; }
    public void setTopRevenueCategory(String topRevenueCategory) { this.topRevenueCategory = topRevenueCategory; }

    public BigDecimal getTotalCouponDiscount() { return totalCouponDiscount; }
    public void setTotalCouponDiscount(BigDecimal totalCouponDiscount) { this.totalCouponDiscount = totalCouponDiscount; }
}
