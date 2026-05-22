package model;

import java.time.LocalDateTime;

/**
 * Represents the RATING table.
 * restaurantId is NOT stored here – derived via Rating → Order → Restaurant (3NF).
 */
public class Rating {

    private int           ratingId;
    private int           score;          // 1 – 5
    private String        comment;
    private LocalDateTime createdAt;
    private String        customerId;
    private int           orderId;

    public Rating() {}

    public int           getRatingId()              { return ratingId; }
    public void          setRatingId(int v)          { this.ratingId = v; }
    public int           getScore()                  { return score; }
    public void          setScore(int v)             { this.score = v; }
    public String        getComment()                { return comment; }
    public void          setComment(String v)        { this.comment = v; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void          setCreatedAt(LocalDateTime v){ this.createdAt = v; }
    public String        getCustomerId()             { return customerId; }
    public void          setCustomerId(String v)     { this.customerId = v; }
    public int           getOrderId()                { return orderId; }
    public void          setOrderId(int v)           { this.orderId = v; }
}