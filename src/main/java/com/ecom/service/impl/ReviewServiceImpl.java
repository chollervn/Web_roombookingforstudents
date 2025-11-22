package com.ecom.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.Review;
import com.ecom.repository.ReviewRepository;
import com.ecom.service.ReviewService;

@Service
public class ReviewServiceImpl implements ReviewService {

	@Autowired
	private ReviewRepository reviewRepository;

	@Override
	public Review saveReview(Review review) {
		return reviewRepository.save(review);
	}

	@Override
	public List<Review> getReviewsByRoomId(Integer roomId) {
		return reviewRepository.findByRoomIdOrderByCreatedDateDesc(roomId);
	}

	@Override
	public List<Review> getReviewsByUserId(Integer userId) {
		return reviewRepository.findByUserId(userId);
	}

	@Override
	public Long getReviewCountByRoomId(Integer roomId) {
		return reviewRepository.countByRoomId(roomId);
	}

	@Override
	public Double getAverageRatingByRoomId(Integer roomId) {
		List<Review> reviews = reviewRepository.findByRoomIdOrderByCreatedDateDesc(roomId);
		if (reviews.isEmpty()) {
			return 0.0;
		}
		double sum = reviews.stream()
				.mapToInt(Review::getRating)
				.sum();
		return sum / reviews.size();
	}

	@Override
	public Boolean hasUserReviewedRoom(Integer roomId, Integer userId) {
		return reviewRepository.existsByRoomIdAndUserId(roomId, userId);
	}

	@Override
	public void deleteReview(Integer reviewId) {
		reviewRepository.deleteById(reviewId);
	}

	@Override
	public Review getReviewById(Integer reviewId) {
		return reviewRepository.findById(reviewId).orElse(null);
	}
	
	@Override
	public Review updateOwnerResponse(Integer reviewId, String response) {
		Review review = reviewRepository.findById(reviewId).orElse(null);
		if (review != null) {
			review.setOwnerResponse(response);
			review.setResponseDate(java.time.LocalDateTime.now());
			return reviewRepository.save(review);
		}
		return null;
	}
	
	@Override
	public List<Review> getReviewsByOwnerId(Integer ownerId) {
		return reviewRepository.findByOwnerId(ownerId);
	}
}

