package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Represents the COUPON table. */
public class Coupon {

    private int        couponId;
    private String     code;
    private String     discountType;   // "percentage" | "fixed"
    private BigDecimal discountValue;
    private LocalDate  validFrom;
    private LocalDate  validUntil;
    private boolean    isActive;
    private int        restaurantId;

    public Coupon() {}

    public int        getCouponId()               { return couponId; }
    public void       setCouponId(int v)           { this.couponId = v; }
    public String     getCode()                    { return code; }
    public void       setCode(String v)            { this.code = v; }
    public String     getDiscountType()            { return discountType; }
    public void       setDiscountType(String v)    { this.discountType = v; }
    public BigDecimal getDiscountValue()           { return discountValue; }
    public void       setDiscountValue(BigDecimal v){ this.discountValue = v; }
    public LocalDate  getValidFrom()               { return validFrom; }
    public void       setValidFrom(LocalDate v)    { this.validFrom = v; }
    public LocalDate  getValidUntil()              { return validUntil; }
    public void       setValidUntil(LocalDate v)   { this.validUntil = v; }
    public boolean    isActive()                   { return isActive; }
    public void       setActive(boolean v)         { this.isActive = v; }
    public int        getRestaurantId()            { return restaurantId; }
    public void       setRestaurantId(int v)       { this.restaurantId = v; }
}