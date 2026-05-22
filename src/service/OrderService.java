package service;

import model.Order;
import model.OrderItem;
import model.Coupon;
import repository.OrderRepository;
import repository.CouponRepository;

import java.math.BigDecimal;
import java.util.List;

public class OrderService {

    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;

    public OrderService() {
        this.orderRepository = new OrderRepository();
        this.couponRepository = new CouponRepository();
    }

    /**
     * Places an order with coupon validation and total amount calculation.
     */
    public boolean placeOrder(Order order, List<OrderItem> items) {
        // 1. Single restaurant constraint is handled by DB trigger, 
        // but we should ideally check it here too if we want a better error message.

        // 2. Validate Coupon if present
        if (order.getCouponId() > 0) {
            Coupon coupon = couponRepository.getValidCoupon(order.getCouponId(), order.getRestaurantId());
            if (coupon == null) {
                System.err.println("Invalid or expired coupon.");
                return false;
            }
            // Calculate discount (simplified logic)
            BigDecimal subtotal = items.stream()
                .map(i -> i.getUnitPrice().multiply(new BigDecimal(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal discount = BigDecimal.ZERO;
            if ("percentage".equalsIgnoreCase(coupon.getDiscountType())) {
                discount = subtotal.multiply(coupon.getDiscountValue().divide(new BigDecimal(100)));
            } else {
                discount = coupon.getDiscountValue();
            }
            order.setDiscountApplied(discount);
            order.setTotalAmount(subtotal.subtract(discount).max(BigDecimal.ZERO));
        } else {
            BigDecimal subtotal = items.stream()
                .map(i -> i.getUnitPrice().multiply(new BigDecimal(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setDiscountApplied(BigDecimal.ZERO);
            order.setTotalAmount(subtotal);
        }

        return orderRepository.placeOrder(order, items);
    }
}
