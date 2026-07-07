package com.app.filmorate.storage.search;

import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import com.app.filmorate.model.Film;
import com.app.filmorate.model.search.SearchTarget;
import com.app.filmorate.storage.film.FilmDbStorage.FilmRowMapper;

@Primary
@Component
@RequiredArgsConstructor
public class SearchDb implements Search {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public List<Film> searchFilms(String searchQuery, Set<SearchTarget> searchTargetSet) {
        StringBuilder query = new StringBuilder(SearchSqlQueries.SEARCH_BASE.getQuery());

        MapSqlParameterSource params = new MapSqlParameterSource("searchQuery", "%" + searchQuery + "%");

        for (SearchTarget searchTarget : searchTargetSet) {
            switch (searchTarget) {
                case TITLE -> {
                    query.append(SearchSqlQueries.SEARCH_BY_TITLE.getQuery());
                }
                case DIRECTOR -> {
                    query.append(SearchSqlQueries.SEARCH_BY_DIRECTOR.getQuery());
                }
            }
        }
        query.append(SearchSqlQueries.SEARCH_SUFFIX.getQuery());

        return namedParameterJdbcTemplate.query(query.toString(), params, filmRowMapper);
    }
}