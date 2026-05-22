package service;

import model.Rating;
import repository.RatingRepository;

import java.time.LocalDateTime;
import java.time.Duration;

public class RatingService {

    private final RatingRepository ratingRepository;

    public RatingService() {
        this.ratingRepository = new RatingRepository();
    }

    /**
     * Enforces §5.8: Customers can rate a restaurant only within 24 hours of the order being accepted.
     */
    public boolean submitRating(Rating rating) {
        LocalDateTime acceptanceTime = ratingRepository.getAcceptanceTime(rating.getOrderId());
        if (acceptanceTime == null) {
            System.err.println("Order has not been accepted yet.");
            return false;
        }

        long hoursSinceAcceptance = Duration.between(acceptanceTime, LocalDateTime.now()).toHours();
        if (hoursSinceAcceptance > 24) {
            System.err.println("Rating window (24h) has expired.");
            return false;
        }

        return ratingRepository.submitRating(rating);
    }
}
