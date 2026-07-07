package com.app.filmorate.storage.film;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilmSqlQueries {
    FIND_ALL("""
            SELECT * FROM film;
            """),
    FIND_BY_ID("""
            SELECT * FROM film WHERE id = :id;
            """),
    CREATE("""
            INSERT INTO film (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID)
            VALUES(:name, :description, :releaseDate, :duration, :mpaId);
            """),
    UPDATE_WITH_MPA("""
            UPDATE film f
            SET name = :name,
                description = :description,
                release_date = :releaseDate,
                duration = :duration,
                mpa_id = :mpaId
            WHERE f.id = :id;
            """),
    UPDATE_NO_MPA("""
            UPDATE film f
            SET name = :name,
                description = :description,
                release_date = :releaseDate,
                duration = :duration
            WHERE f.id = :id;
            """),
    DELETE("""
            DELETE FROM film WHERE id = :id
            """),
    FIND_TOP_LIKED("""
            SELECT f.*
            FROM film AS f
            LEFT JOIN "like" AS l ON f.id = l.film_id
            GROUP BY f.id
            ORDER BY COUNT(l.id) DESC
            LIMIT :size;
            """),
    // Части динамического запроса
    FIND_TOP_LIKED_DYNAMIC_BASE("""
            SELECT f.*, COUNT(l.id) as like_count
            FROM film AS f
            LEFT JOIN "like" AS l ON f.id = l.film_id
            LEFT JOIN film_genre AS fg ON f.id = fg.film_id
            """),
    FIND_TOP_LIKED_GENRE_CONDITION("""
            fg.genre_id = :genreId
            """),
    FIND_TOP_LIKED_YEAR_CONDITION("""
            EXTRACT(YEAR FROM f.release_date) = :year
            """),
    FIND_TOP_LIKED_DYNAMIC_SUFFIX("""
            GROUP BY f.id
            ORDER BY like_count DESC
            LIMIT :count
            """),

    FIND_BY_DIRECTOR("""
            SELECT f.* from film f
            JOIN film_director fd on f.id = fd.film_id
            WHERE fd.director_id = :directorId;
            """),
    FIND_BY_DIRECTOR_SORT_BY_LIKES("""
            SELECT f.* FROM film f
            LEFT JOIN film_director fd ON f.id = fd.film_id
            LEFT JOIN "like" l ON f.id = l.film_id
            WHERE fd.director_id = :directorId
            GROUP BY f.id
            ORDER BY count(DISTINCT l.user_id) DESC;
            """),
    FIND_BY_DIRECTOR_SORT_BY_YEAR("""
            SELECT f.* FROM film f
            JOIN film_director fd ON f.id = fd.film_id
            WHERE fd.director_id = :directorId
            ORDER BY f.release_date ASC;
            """),
    FIND_COMMON_FILMS("""
            SELECT f.*
            FROM film f
            JOIN "like" l_user ON f.id = l_user.film_id AND l_user.user_id = :userId
            JOIN "like" l_friend ON f.id = l_friend.film_id AND l_friend.user_id = :friendId
            LEFT JOIN "like" l_all ON f.id = l_all.film_id
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
            ORDER BY COUNT(l_all.user_id) DESC, f.id;
            """);

    private final String query;
}