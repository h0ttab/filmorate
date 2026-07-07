package com.app.filmorate.storage.like;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LikeSqlQueries {
    ADD_LIKE("""
            INSERT INTO "like" (film_id, user_id)
            VALUES (:filmId, :userId);
            """),
    REMOVE_LIKE("""
            DELETE FROM "like"
            WHERE film_id = :filmId
            AND user_id = :userId;
            """),
    GET_LIKES_BY_FILM_ID("""
            SELECT user_id FROM "like"
            WHERE film_id = :filmId;
            """),
    GET_LIKES_BY_FILM_ID_LIST("""
            SELECT
                film_id,
                user_id
            FROM "like"
            WHERE film_id in (:filmIds)
            ORDER BY film_id;
            """);

    private final String query;
}