package com.app.filmorate.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.app.filmorate.mapper.ReviewMapper;
import com.app.filmorate.model.Review;
import com.app.filmorate.model.dto.review.ReviewCreateDto;
import com.app.filmorate.model.dto.review.ReviewUpdateDto;
import com.app.filmorate.storage.review.ReviewStorage;
import com.app.filmorate.util.DtoHelper;
import com.app.filmorate.util.Validators;

import static com.app.filmorate.model.FeedEventType.REVIEW;
import static com.app.filmorate.model.OperationType.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FeedService feedService;
    private final Validators validators;
    private final ReviewMapper mapper;
    private final DtoHelper dtoHelper;

    public Review create(ReviewCreateDto dto) {
        Review review = mapper.toEntity(dto);
        Review createReview = reviewStorage.create(review);
        feedService.save(createReview.getUserId(), REVIEW, ADD, createReview.getReviewId());
        return createReview;
    }

    public Review update(ReviewUpdateDto dto) {
        validators.validateReviewExists(dto.getReviewId(), getClass());
        Review original = reviewStorage.findById(dto.getReviewId());
        Review patch = mapper.toEntity(dto);
        Review merged = (Review) dtoHelper.transferFields(original, patch);
        Review updateReview = reviewStorage.update(merged);
        feedService.save(updateReview.getUserId(), REVIEW, UPDATE, updateReview.getReviewId());
        return updateReview;
    }

    public void delete(Integer id) {
        validators.validateReviewExists(id, getClass());
        Review deleteReview = reviewStorage.findById(id);
        feedService.save(deleteReview.getUserId(), REVIEW, REMOVE, id);
        reviewStorage.delete(id);
    }

    public Review findById(Integer id) {
        validators.validateReviewExists(id, getClass());
        return reviewStorage.findById(id);
    }

    public List<Review> findAll(Integer filmId, Integer count) {
        int limit = (count == null || count <= 0) ? 10 : count;
        if (filmId != null) {
            validators.validateFilmExists(filmId, getClass());
        }
        return reviewStorage.findAll(filmId, limit);
    }

    public void addLike(Integer reviewId, Integer userId) {
        validators.validateReviewExists(reviewId, getClass());
        validators.validateUserExists(userId, getClass());
        reviewStorage.addUseful(reviewId, userId);
    }

    public void addDislike(Integer reviewId, Integer userId) {
        validators.validateReviewExists(reviewId, getClass());
        validators.validateUserExists(userId, getClass());
        reviewStorage.addUseless(reviewId, userId);
    }

    public void removeLike(Integer reviewId, Integer userId) {
        validators.validateReviewFeedbackExists(reviewId, userId, getClass());
        reviewStorage.removeUseful(reviewId, userId);
    }

    public void removeDislike(Integer reviewId, Integer userId) {
        validators.validateReviewFeedbackExists(reviewId, userId, getClass());
        reviewStorage.removeUseless(reviewId, userId);
    }
}