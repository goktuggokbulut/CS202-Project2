package model;

import java.time.LocalDateTime;

/**
 * Represents the ORDER_STATUS_HISTORY weak entity.
 * Composite PK: (orderId, status).
 * Partial key (discriminator): status.
 */
public class OrderStatusHistory {

    private int           orderId;
    private String        status;
    private LocalDateTime timeStamp;

    public OrderStatusHistory() {}

    public int           getOrderId()               { return orderId; }
    public void          setOrderId(int v)           { this.orderId = v; }
    public String        getStatus()                 { return status; }
    public void          setStatus(String v)         { this.status = v; }
    public LocalDateTime getTimeStamp()              { return timeStamp; }
    public void          setTimeStamp(LocalDateTime v){ this.timeStamp = v; }
}