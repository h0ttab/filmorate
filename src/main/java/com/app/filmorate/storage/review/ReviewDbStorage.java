package com.app.filmorate.storage.review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import com.app.filmorate.exception.ExceptionType;
import com.app.filmorate.exception.LoggedException;
import com.app.filmorate.model.Review;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<Review> mapper = new ReviewRowMapper();

    @Override
    public Review create(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("isPositive", review.getIsPositive())
                .addValue("userId", review.getUserId())
                .addValue("filmId", review.getFilmId())
                .addValue("useful", review.getUseful());

        namedParameterJdbcTemplate.update(ReviewSqlQueries.CREATE.getQuery(), params, keyHolder, new String[]{"id"});

        if (Optional.ofNullable(keyHolder.getKey()).isEmpty()) {
            LoggedException.throwNew(ExceptionType.UNEXPECTED_ERROR, getClass(), List.of());
        }

        review.setReviewId(keyHolder.getKey().intValue());
        return review;
    }

    @Override
    public Review update(Review review) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("isPositive", review.getIsPositive())
                .addValue("id", review.getReviewId());

        namedParameterJdbcTemplate.update(ReviewSqlQueries.UPDATE.getQuery(), params);
        return review;
    }

    @Override
    public Integer delete(Integer id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        int deleted = namedParameterJdbcTemplate.update(ReviewSqlQueries.DELETE.getQuery(), params);
        if (deleted == 0) {
            LoggedException.throwNew(ExceptionType.REVIEW_NOT_FOUND, getClass(), List.of(id));
        }
        return id;
    }

    @Override
    public Review findById(Integer id) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        List<Review> result = namedParameterJdbcTemplate.query(ReviewSqlQueries.FIND_BY_ID.getQuery(), params, mapper);
        if (result.isEmpty()) {
            LoggedException.throwNew(ExceptionType.REVIEW_NOT_FOUND, getClass(), List.of(id));
        }
        return result.getFirst();
    }

    @Override
    public List<Review> findAll(Integer filmId, int count) {
        StringBuilder query = new StringBuilder(ReviewSqlQueries.FIND_ALL_BASE.getQuery());
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (filmId != null) {
            query.append(ReviewSqlQueries.FIND_ALL_FILM_CONDITION.getQuery());
            params.addValue("filmId", filmId);
        }
        query.append(ReviewSqlQueries.FIND_ALL_SUFFIX.getQuery());
        params.addValue("count", count);

        return namedParameterJdbcTemplate.query(query.toString(), params, mapper);
    }

    @Override
    public void addUseful(Integer reviewId, Integer userId) {
        applyReaction(reviewId, userId, true);
    }

    @Override
    public void addUseless(Integer reviewId, Integer userId) {
        applyReaction(reviewId, userId, false);
    }

    @Override
    public void removeUseful(Integer reviewId, Integer userId) {
        removeReaction(reviewId, userId, true);
    }

    @Override
    public void removeUseless(Integer reviewId, Integer userId) {
        removeReaction(reviewId, userId, false);
    }

    private void applyReaction(Integer reviewId, Integer userId, boolean isUseful) {
        int delta = tryFlipReaction(reviewId, userId, isUseful);

        if (delta == 0) {
            delta = tryInsertReaction(reviewId, userId, isUseful);
        }

        if (delta != 0) {
            updateUseful(reviewId, delta);
        }
    }

    /**
     * Пытается переключить существующую реакцию пользователя.
     * Возвращает +2 / -2, если реакция была переключена, или 0, если ничего не изменилось.
     */
    private int tryFlipReaction(Integer reviewId, Integer userId, boolean isUseful) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("isUseful", isUseful)
                .addValue("reviewId", reviewId)
                .addValue("userId", userId);

        int updated = namedParameterJdbcTemplate.update(ReviewSqlQueries.FLIP_REACTION.getQuery(), params);

        if (updated == 1) {
            // дизлайк→лайк: +2; лайк→дизлайк: -2
            return isUseful ? +2 : -2;
        }

        return 0;
    }

    /**
     * Пытается вставить новую реакцию, если её ещё не было.
     * Возвращает +1 / -1, если реакция добавлена, или 0, если ничего не изменилось.
     */
    private int tryInsertReaction(Integer reviewId, Integer userId, boolean isUseful) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reviewId", reviewId)
                .addValue("userId", userId)
                .addValue("isUseful", isUseful);

        int inserted = namedParameterJdbcTemplate.update(ReviewSqlQueries.INSERT_REACTION.getQuery(), params);

        if (inserted == 1) {
            // новая реакция: лайк +1, дизлайк -1
            return isUseful ? +1 : -1;
        }

        return 0;
    }

    /**
     * Применяет изменение к счётчику полезности отзыва.
     */
    private void updateUseful(Integer reviewId, int delta) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("delta", delta)
                .addValue("id", reviewId);
        namedParameterJdbcTemplate.update(ReviewSqlQueries.UPDATE_USEFUL.getQuery(), params);
    }

    private void removeReaction(Integer reviewId, Integer userId, boolean isUseful) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reviewId", reviewId)
                .addValue("userId", userId)
                .addValue("isUseful", isUseful);

        int deleted = namedParameterJdbcTemplate.update(ReviewSqlQueries.REMOVE_REACTION.getQuery(), params);

        if (deleted == 1) {
            // сняли лайк → -1; сняли дизлайк → +1
            int delta = isUseful ? -1 : +1;
            updateUseful(reviewId, delta);
        }
    }

    private static class ReviewRowMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Review.builder()
                    .reviewId(rs.getInt("id"))
                    .content(rs.getString("content"))
                    .isPositive(rs.getBoolean("is_positive"))
                    .userId(rs.getInt("user_id"))
                    .filmId(rs.getInt("film_id"))
                    .useful(rs.getInt("useful"))
                    .build();
        }
    }
}