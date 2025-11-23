package ru.yandex.practicum.filmorate.storage.film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorage implements FilmStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmRowMapper mapper;

    @Override
    public List<Film> findAll() {
        return namedParameterJdbcTemplate.query(FilmSqlQueries.FIND_ALL.getQuery(), mapper);
    }

    @Override
    public Film findById(Integer filmId) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", filmId);
        List<Film> result = namedParameterJdbcTemplate.query(FilmSqlQueries.FIND_BY_ID.getQuery(), params, mapper);
        if (result.isEmpty()) {
            LoggedException.throwNew(ExceptionType.FILM_NOT_FOUND, getClass(), List.of(filmId));
        }
        return result.getFirst();
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("mpaId", film.getMpa().getId());

        namedParameterJdbcTemplate.update(FilmSqlQueries.CREATE.getQuery(), params, keyHolder, new String[]{"id"});

        if (Optional.ofNullable(keyHolder.getKey()).isEmpty()) {
            LoggedException.throwNew(ExceptionType.UNEXPECTED_ERROR, getClass(), List.of());
        }

        film.setId(keyHolder.getKey().intValue());
        log.info("Добавлен новый фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        int updatedFilmRows;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("id", film.getId());

        if (film.getMpa() == null) {
            updatedFilmRows = namedParameterJdbcTemplate.update(FilmSqlQueries.UPDATE_NO_MPA.getQuery(), params);
        } else {
            params.addValue("mpaId", film.getMpa().getId());
            updatedFilmRows = namedParameterJdbcTemplate.update(FilmSqlQueries.UPDATE_WITH_MPA.getQuery(), params);
        }
        if (updatedFilmRows == 0) {
            LoggedException.throwNew(ExceptionType.FILM_NOT_FOUND, getClass(), List.of(film.getId()));
        }
        log.info("Обновлён фильм id {}. Новое значение: {}", film.getId(), film);
        return film;
    }

    @Override
    public Integer delete(Integer filmId) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", filmId);
        int deletedRows = namedParameterJdbcTemplate.update(FilmSqlQueries.DELETE.getQuery(), params);
        if (deletedRows == 0) {
            LoggedException.throwNew(ExceptionType.FILM_NOT_FOUND, getClass(), List.of(filmId));
        }
        log.info("Удалён фильм id {}", filmId);
        return filmId;
    }

    @Override
    public List<Film> findTopLiked(int size) {
        MapSqlParameterSource params = new MapSqlParameterSource("size", size);
        return namedParameterJdbcTemplate.query(FilmSqlQueries.FIND_TOP_LIKED.getQuery(), params, mapper);
    }

    @Override
    public List<Film> findTopLiked(int count, Integer genreId, Integer year) {
        StringBuilder queryBuilder = new StringBuilder(FilmSqlQueries.FIND_TOP_LIKED_DYNAMIC_BASE.getQuery());

        // Начинаем формировать условия WHERE
        MapSqlParameterSource params = new MapSqlParameterSource();
        boolean hasConditions = false;

        // Добавляем условие по жанру, если указан genreId
        if (genreId != null) {
            queryBuilder.append("WHERE ").append(FilmSqlQueries.FIND_TOP_LIKED_GENRE_CONDITION.getQuery());
            params.addValue("genreId", genreId);
            hasConditions = true;
        }

        // Добавляем условие по году, если указан year
        if (year != null) {
            if (hasConditions) {
                queryBuilder.append("AND ");
            } else {
                queryBuilder.append("WHERE ");
            }
            queryBuilder.append(FilmSqlQueries.FIND_TOP_LIKED_YEAR_CONDITION.getQuery());
            params.addValue("year", year);
        }

        // Добавляем группировку, сортировку и лимит
        queryBuilder.append(FilmSqlQueries.FIND_TOP_LIKED_DYNAMIC_SUFFIX.getQuery());

        // Добавляем параметр limit
        params.addValue("count", count);

        return namedParameterJdbcTemplate.query(queryBuilder.toString(), params, mapper);
    }

    @Override
    public List<Film> findByDirector(Integer directorId, SortOrder order) {
        String query = FilmSqlQueries.FIND_BY_DIRECTOR.getQuery();

        switch (order) {
            case LIKES -> {
                query = FilmSqlQueries.FIND_BY_DIRECTOR_SORT_BY_LIKES.getQuery();
            }
            case YEAR -> {
                query = FilmSqlQueries.FIND_BY_DIRECTOR_SORT_BY_YEAR.getQuery();
            }
        }
        MapSqlParameterSource params = new MapSqlParameterSource("directorId", directorId);
        return namedParameterJdbcTemplate.query(query, params, mapper);
    }

    @Override
    public List<Film> findCommonFilms(Integer userId, Integer friendId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        return namedParameterJdbcTemplate.query(FilmSqlQueries.FIND_COMMON_FILMS.getQuery(), params, mapper);
    }

    @Component
    @RequiredArgsConstructor
    public static class FilmRowMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return Film.builder()
                    .id(resultSet.getInt("ID"))
                    .name(resultSet.getString("NAME"))
                    .description(resultSet.getString("DESCRIPTION"))
                    .releaseDate(resultSet.getDate("RELEASE_DATE").toLocalDate())
                    .duration(resultSet.getInt("DURATION"))
                    .build();
        }
    }
}