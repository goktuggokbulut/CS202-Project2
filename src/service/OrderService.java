package service;

import model.Coupon;
import model.Order;
import model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.CouponRepository;
import repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CouponRepository couponRepository;

    public boolean placeOrder(Order order, List<OrderItem> items) {
        if (order.getCouponId() != null && order.getCouponId() > 0) {
            Coupon coupon = couponRepository.getValidCoupon(order.getCouponId(), order.getRestaurantId());
            if (coupon == null) {
                System.err.println("Invalid or expired coupon.");
                return false;
            }
            BigDecimal subtotal = items.stream()
                .map(i -> i.getUnitPrice().multiply(new BigDecimal(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal discount;
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
