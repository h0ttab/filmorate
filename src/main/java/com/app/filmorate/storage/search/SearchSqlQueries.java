package com.app.filmorate.storage.search;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchSqlQueries {
    SEARCH_BASE("""
            SELECT f.* FROM film f
            LEFT JOIN film_director fd ON fd.film_id = f.id
            LEFT JOIN director dir ON fd.director_id = dir.id
            LEFT JOIN "like" AS l ON f.id = l.film_id
            WHERE FALSE
            """),
    SEARCH_BY_TITLE("""
            OR f.name ILIKE :searchQuery
            """),
    SEARCH_BY_DIRECTOR("""
            OR dir.name ILIKE :searchQuery
            """),
    SEARCH_SUFFIX("""
            GROUP BY f.id
            ORDER BY COUNT(l.id) DESC;
            """);

    private final String query;
}