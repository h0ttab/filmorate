package ru.yandex.practicum.filmorate.storage.recommendation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecommendationSqlQueries {
    GET_LIKED_FILMS_BY_USER_ID("""
            SELECT film_id FROM "like"
            WHERE user_id = :userId;
            """),
    GET_RECOMMENDATIONS("""
            WITH user_likes AS (
                SELECT film_id
                FROM "like"
                WHERE user_id = :userId
            ),
            similar_users AS (
                SELECT l.user_id, COUNT(*) AS common_likes
                FROM "like" l
                JOIN user_likes ul ON l.film_id = ul.film_id
                WHERE l.user_id != :userId
                GROUP BY l.user_id
            ),
            recommendations AS (
                SELECT
                    l.film_id,
                    MAX(su.common_likes) AS score
                FROM "like" l
                JOIN similar_users su ON l.user_id = su.user_id
                WHERE l.film_id NOT IN (SELECT film_id FROM user_likes)
                GROUP BY l.film_id
            )
            SELECT f.*
            FROM film f
            JOIN recommendations r ON f.id = r.film_id
            ORDER BY r.score DESC, f.id
            LIMIT 10;
            """);

    private final String query;
}