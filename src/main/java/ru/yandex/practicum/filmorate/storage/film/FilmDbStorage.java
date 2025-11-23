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
        String query = "SELECT * FROM film;";
        return namedParameterJdbcTemplate.query(query, mapper);
    }

    @Override
    public Film findById(Integer filmId) {
        String query = "SELECT * FROM film WHERE id = :id;";
        MapSqlParameterSource params = new MapSqlParameterSource("id", filmId);
        List<Film> result = namedParameterJdbcTemplate.query(query, params, mapper);
        if (result.isEmpty()) {
            LoggedException.throwNew(ExceptionType.FILM_NOT_FOUND, getClass(), List.of(filmId));
        }
        return result.getFirst();
    }

    @Override
    public Film create(Film film) {
        String query = """
                INSERT INTO film (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID)
                VALUES(:name, :description, :releaseDate, :duration, :mpaId);
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("mpaId", film.getMpa().getId());

        namedParameterJdbcTemplate.update(query, params, keyHolder, new String[]{"id"});

        if (Optional.ofNullable(keyHolder.getKey()).isEmpty()) {
            LoggedException.throwNew(ExceptionType.UNEXPECTED_ERROR, getClass(), List.of());
        }

        film.setId(keyHolder.getKey().intValue());
        log.info("Добавлен новый фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {

        String queryFilmUpdateWithMpa = """
                    UPDATE film f
                    SET name = :name,
                        description = :description,
                        release_date = :releaseDate,
                        duration = :duration,
                        mpa_id = :mpaId
                    WHERE f.id = :id;
                """;
        String queryFilmUpdateNoMpa = """
                UPDATE film f
                SET name = :name,
                    description = :description,
                    release_date = :releaseDate,
                    duration = :duration
                WHERE f.id = :id;
                """;
        int updatedFilmRows;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("id", film.getId());

        if (film.getMpa() == null) {
            updatedFilmRows = namedParameterJdbcTemplate.update(queryFilmUpdateNoMpa, params);
        } else {
            params.addValue("mpaId", film.getMpa().getId());
            updatedFilmRows = namedParameterJdbcTemplate.update(queryFilmUpdateWithMpa, params);
        }
        if (updatedFilmRows == 0) {
            LoggedException.throwNew(ExceptionType.FILM_NOT_FOUND, getClass(), List.of(film.getId()));
        }
        log.info("Обновлён фильм id {}. Новое значение: {}", film.getId(), film);
        return film;
    }

    @Override
    public Integer delete(Integer filmId) {
        String query = "DELETE FROM film WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", filmId);
        int deletedRows = namedParameterJdbcTemplate.update(query, params);
        if (deletedRows == 0) {
            LoggedException.throwNew(ExceptionType.FILM_NOT_FOUND, getClass(), List.of(filmId));
        }
        log.info("Удалён фильм id {}", filmId);
        return filmId;
    }

    @Override
    public List<Film> findTopLiked(int size) {
        String query = """
                    SELECT f.*
                    FROM film AS f
                    LEFT JOIN "like" AS l ON f.id = l.film_id
                    GROUP BY f.id
                    ORDER BY COUNT(l.id) DESC
                    LIMIT :size;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("size", size);
        return namedParameterJdbcTemplate.query(query, params, mapper);
    }

    @Override
    public List<Film> findTopLiked(int count, Integer genreId, Integer year) {
        StringBuilder queryBuilder = new StringBuilder("""
                SELECT f.*, COUNT(l.id) as like_count
                FROM film AS f
                LEFT JOIN "like" AS l ON f.id = l.film_id
                LEFT JOIN film_genre AS fg ON f.id = fg.film_id
                """);

        // Начинаем формировать условия WHERE
        MapSqlParameterSource params = new MapSqlParameterSource();
        boolean hasConditions = false;

        // Добавляем условие по жанру, если указан genreId
        if (genreId != null) {
            queryBuilder.append("WHERE fg.genre_id = :genreId ");
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
            queryBuilder.append("EXTRACT(YEAR FROM f.release_date) = :year ");
            params.addValue("year", year);
        }

        // Добавляем группировку, сортировку и лимит
        queryBuilder.append("""
                GROUP BY f.id
                ORDER BY like_count DESC
                LIMIT :count
                """);

        // Добавляем параметр limit
        params.addValue("count", count);

        return namedParameterJdbcTemplate.query(queryBuilder.toString(), params, mapper);
    }

    @Override
    public List<Film> findByDirector(Integer directorId, SortOrder order) {
        String query = """
                SELECT f.* from film f
                JOIN film_director fd on f.id = fd.film_id
                WHERE fd.director_id = :directorId;
                """;

        switch (order) {
            case LIKES -> {
                query = """
                        SELECT f.* FROM film f
                        LEFT JOIN film_director fd ON f.id = fd.film_id
                        LEFT JOIN "like" l ON f.id = l.film_id
                        WHERE fd.director_id = :directorId
                        GROUP BY f.id
                        ORDER BY count(DISTINCT l.user_id) DESC;
                        """;
            }
            case YEAR -> {
                query = """
                        SELECT f.* FROM film f
                        JOIN film_director fd ON f.id = fd.film_id
                        WHERE fd.director_id = :directorId
                        ORDER BY f.release_date ASC;
                        """;
            }
        }
        MapSqlParameterSource params = new MapSqlParameterSource("directorId", directorId);
        return namedParameterJdbcTemplate.query(query, params, mapper);
    }

    @Override
    public List<Film> findCommonFilms(Integer userId, Integer friendId) {
        String query = """
                SELECT f.*
                FROM film f
                JOIN "like" l_user ON f.id = l_user.film_id AND l_user.user_id = :userId
                JOIN "like" l_friend ON f.id = l_friend.film_id AND l_friend.user_id = :friendId
                LEFT JOIN "like" l_all ON f.id = l_all.film_id
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                ORDER BY COUNT(l_all.user_id) DESC, f.id;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        return namedParameterJdbcTemplate.query(query, params, mapper);
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