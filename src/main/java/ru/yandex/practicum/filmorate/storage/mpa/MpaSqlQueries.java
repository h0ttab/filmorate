package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MpaSqlQueries {
    FIND_ALL("""
            SELECT * FROM mpa
            ORDER BY id;
            """),
    FIND_BY_ID("""
            SELECT * FROM mpa
            WHERE id = :id;
            """),
    FIND_BY_FILM_ID("""
            SELECT * FROM mpa
            JOIN film f on f.mpa_id = mpa.id
            WHERE f.id = :filmId;
            """),
    FIND_BY_ID_SET("""
            SELECT * FROM mpa
            WHERE id in (:mpaIdList);
            """),
    FIND_BY_FILM_ID_LIST("""
            SELECT
                f.id AS film_id, m.id AS mpa_id, m.name AS mpa_name
            FROM mpa m
            JOIN film f on f.mpa_id = m.id
            WHERE f.id in (:filmIds);
            """);

    private final String query;
}