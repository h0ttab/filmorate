package ru.yandex.practicum.filmorate.storage.search;

import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.search.SearchTarget;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage.FilmRowMapper;

@Primary
@Component
@RequiredArgsConstructor
public class SearchDb implements Search {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public List<Film> searchFilms(String searchQuery, Set<SearchTarget> searchTargetSet) {
        StringBuilder query = new StringBuilder("""
                SELECT f.* FROM film f
                LEFT JOIN film_director fd ON fd.film_id = f.id
                LEFT JOIN director dir ON fd.director_id = dir.id
                LEFT JOIN "like" AS l ON f.id = l.film_id
                WHERE FALSE
                """);

        MapSqlParameterSource params = new MapSqlParameterSource("searchQuery", "%" + searchQuery + "%");

        for (SearchTarget searchTarget : searchTargetSet) {
            switch (searchTarget) {
                case TITLE -> {
                    query.append("OR f.name ILIKE :searchQuery \n");
                }
                case DIRECTOR -> {
                    query.append("OR dir.name ILIKE :searchQuery \n");
                }
            }
        }
        query.append("""
                GROUP BY f.id
                ORDER BY COUNT(l.id) DESC;
                """);

        return namedParameterJdbcTemplate.query(query.toString(), params, filmRowMapper);
    }
}