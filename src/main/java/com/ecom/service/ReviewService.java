package com.ecom.service;

import java.util.List;

import com.ecom.model.Review;

public interface ReviewService {

	Review saveReview(Review review);

	List<Review> getReviewsByRoomId(Integer roomId);

	List<Review> getReviewsByUserId(Integer userId);

	Long getReviewCountByRoomId(Integer roomId);

	Double getAverageRatingByRoomId(Integer roomId);

	Boolean hasUserReviewedRoom(Integer roomId, Integer userId);

	void deleteReview(Integer reviewId);

	Review getReviewById(Integer reviewId);

	Review updateOwnerResponse(Integer reviewId, String response);

	List<Review> getReviewsByOwnerId(Integer ownerId);

	List<Review> getAllReviews();
}
