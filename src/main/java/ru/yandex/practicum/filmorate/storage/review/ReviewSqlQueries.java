package ru.yandex.practicum.filmorate.storage.review;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewSqlQueries {
    CREATE("""
            INSERT INTO review (content, is_positive, user_id, film_id, useful)
            VALUES (:content, :isPositive, :userId, :filmId, :useful);
            """),
    UPDATE("""
            UPDATE review
            SET content = :content,
                is_positive = :isPositive
            WHERE id = :id;
            """),
    DELETE("""
            DELETE FROM review WHERE id = :id;
            """),
    FIND_BY_ID("""
            SELECT * FROM review WHERE id = :id;
            """),
    FIND_ALL_BASE("""
            SELECT * FROM review
            """),
    FIND_ALL_FILM_CONDITION("""
             WHERE film_id = :filmId
            """),
    FIND_ALL_SUFFIX("""
             ORDER BY useful DESC, id ASC LIMIT :count;
            """),
    FLIP_REACTION("""
            UPDATE review_feedback
               SET is_useful = :isUseful
             WHERE review_id = :reviewId
               AND user_id   = :userId
               AND is_useful <> :isUseful
            """),
    INSERT_REACTION("""
            INSERT INTO review_feedback (review_id, user_id, is_useful)
            SELECT :reviewId, :userId, :isUseful
             WHERE NOT EXISTS (
                   SELECT 1 FROM review_feedback
                    WHERE review_id = :reviewId
                      AND user_id   = :userId
             )
            """),
    UPDATE_USEFUL("""
            UPDATE review SET useful = useful + :delta WHERE id = :id
            """),
    REMOVE_REACTION("""
            DELETE FROM review_feedback
             WHERE review_id = :reviewId
               AND user_id   = :userId
               AND is_useful = :isUseful
            """);

    private final String query;
}