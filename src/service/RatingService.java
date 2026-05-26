package service;

import model.Rating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.RatingRepository;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

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
