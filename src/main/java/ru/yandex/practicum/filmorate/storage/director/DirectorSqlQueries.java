package ru.yandex.practicum.filmorate.storage.director;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DirectorSqlQueries {
    CREATE("""
            INSERT INTO director (name)
                VALUES (:directorName);
            """),
    UPDATE("""
            UPDATE director
            SET name = :directorName
            WHERE id = :directorId;
            """),
    DELETE("""
            DELETE FROM director
            WHERE id = :directorId;
            """),
    LINK_DELETE("""
            DELETE FROM film_director
            WHERE film_id = :filmId;
            """),
    LINK_INSERT("""
            INSERT INTO film_director(film_id, director_id)
            VALUES (:filmId, :directorId)
            """),
    FIND_ALL("""
            SELECT * FROM director
            ORDER BY id;
            """),
    FIND_BY_ID("""
            SELECT * FROM director
            WHERE id = :directorId;
            """),
    FIND_BY_FILM_ID("""
            SELECT d.* FROM director d
            JOIN film_director fd ON d.id = fd.director_id
            WHERE fd.film_id = :filmId;
            """),
    FIND_BY_ID_LIST("""
            SELECT * FROM director
            WHERE id IN (:ids);
            """),
    FIND_BY_FILM_ID_LIST("""
                SELECT
                    fd.film_id,
                    d.id AS director_id,
                    d.name AS director_name
                FROM film_director fd
                JOIN director d ON d.id = fd.director_id
                WHERE fd.film_id IN (:filmIds)
                ORDER BY fd.film_id;
            """);


    private final String query;
}
