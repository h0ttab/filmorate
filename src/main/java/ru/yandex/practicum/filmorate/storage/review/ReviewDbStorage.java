package ru.yandex.practicum.filmorate.storage.review;

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
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.model.Review;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<Review> mapper = new ReviewRowMapper();

    @Override
    public Review create(Review review) {
        String query = """
                INSERT INTO review (content, is_positive, user_id, film_id, useful)
                VALUES (:content, :isPositive, :userId, :filmId, :useful);
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("isPositive", review.getIsPositive())
                .addValue("userId", review.getUserId())
                .addValue("filmId", review.getFilmId())
                .addValue("useful", review.getUseful());

        namedParameterJdbcTemplate.update(query, params, keyHolder, new String[]{"id"});

        if (Optional.ofNullable(keyHolder.getKey()).isEmpty()) {
            LoggedException.throwNew(ExceptionType.UNEXPECTED_ERROR, getClass(), List.of());
        }

        review.setReviewId(keyHolder.getKey().intValue());
        return review;
    }

    @Override
    public Review update(Review review) {
        String query = """
                UPDATE review
                SET content = :content,
                    is_positive = :isPositive
                WHERE id = :id;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("isPositive", review.getIsPositive())
                .addValue("id", review.getReviewId());

        namedParameterJdbcTemplate.update(query, params);
        return review;
    }

    @Override
    public Integer delete(Integer id) {
        String query = "DELETE FROM review WHERE id = :id;";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        int deleted = namedParameterJdbcTemplate.update(query, params);
        if (deleted == 0) {
            LoggedException.throwNew(ExceptionType.REVIEW_NOT_FOUND, getClass(), List.of(id));
        }
        return id;
    }

    @Override
    public Review findById(Integer id) {
        String query = "SELECT * FROM review WHERE id = :id;";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        List<Review> result = namedParameterJdbcTemplate.query(query, params, mapper);
        if (result.isEmpty()) {
            LoggedException.throwNew(ExceptionType.REVIEW_NOT_FOUND, getClass(), List.of(id));
        }
        return result.getFirst();
    }

    @Override
    public List<Review> findAll(Integer filmId, int count) {
        StringBuilder query = new StringBuilder("SELECT * FROM review");
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (filmId != null) {
            query.append(" WHERE film_id = :filmId");
            params.addValue("filmId", filmId);
        }
        query.append(" ORDER BY useful DESC, id ASC LIMIT :count;");
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
        String query = """
                        UPDATE review_feedback
                           SET is_useful = :isUseful
                         WHERE review_id = :reviewId
                           AND user_id   = :userId
                           AND is_useful <> :isUseful
                        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("isUseful", isUseful)
                .addValue("reviewId", reviewId)
                .addValue("userId", userId);

        int updated = namedParameterJdbcTemplate.update(query, params);

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
        String query = """
                        INSERT INTO review_feedback (review_id, user_id, is_useful)
                        SELECT :reviewId, :userId, :isUseful
                         WHERE NOT EXISTS (
                               SELECT 1 FROM review_feedback
                                WHERE review_id = :reviewId
                                  AND user_id   = :userId
                         )
                        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reviewId", reviewId)
                .addValue("userId", userId)
                .addValue("isUseful", isUseful);

        int inserted = namedParameterJdbcTemplate.update(query, params);

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
        String query = "UPDATE review SET useful = useful + :delta WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("delta", delta)
                .addValue("id", reviewId);
        namedParameterJdbcTemplate.update(query, params);
    }

    private void removeReaction(Integer reviewId, Integer userId, boolean isUseful) {
        String query = """
                        DELETE FROM review_feedback
                         WHERE review_id = :reviewId
                           AND user_id   = :userId
                           AND is_useful = :isUseful
                        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reviewId", reviewId)
                .addValue("userId", userId)
                .addValue("isUseful", isUseful);

        int deleted = namedParameterJdbcTemplate.update(query, params);

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