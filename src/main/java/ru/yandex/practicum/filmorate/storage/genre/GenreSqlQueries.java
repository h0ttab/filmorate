package ru.yandex.practicum.filmorate.storage.genre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenreSqlQueries {
    FIND_ALL("""
            SELECT * FROM genre
            ORDER BY id;
            """),
    FIND_BY_ID("""
            SELECT * FROM genre
            WHERE id = :id;
            """),
    FIND_BY_FILM_ID("""
            SELECT g.*
            FROM genre g
            JOIN film_genre fg ON g.id = fg.genre_id
            WHERE fg.film_id = :filmId
            ORDER BY fg.genre_id;
            """),
    FIND_BY_ID_LIST("""
            SELECT * FROM genre
            WHERE id IN (:ids)
            """),
    FIND_BY_FILM_ID_LIST("""
            SELECT
                fg.film_id,
                g.id AS genre_id,
                g.name AS genre_name
            FROM film_genre fg
            JOIN genre g ON g.id = fg.genre_id
            WHERE fg.film_id IN (:filmIds)
            ORDER BY fg.film_id;
            """),
    LINK_DELETE("""
            DELETE FROM film_genre WHERE film_id = :filmId;
            """),
    LINK_INSERT("""
            INSERT INTO film_genre (film_id, genre_id) VALUES (:filmId, :genreId);
            """);

    private final String query;
}