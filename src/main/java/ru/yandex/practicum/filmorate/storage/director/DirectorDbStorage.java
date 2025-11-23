package ru.yandex.practicum.filmorate.storage.director;

import java.sql.*;
import java.util.*;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.model.Director;

@Primary
@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<Director> mapper;
    private final RowMapper<DirectorBatchDto> batchDirectorMapper;


    @Override
    public Director create(Director director) {
        MapSqlParameterSource params = new MapSqlParameterSource("directorName", director.getName());
        String query = """
                INSERT INTO director (name)
                VALUES (:directorName);
                """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(query, params, keyHolder, new String[]{"id"});

        if (Optional.ofNullable(keyHolder.getKey()).isEmpty()) {
            LoggedException.throwNew(ExceptionType.UNEXPECTED_ERROR, getClass(), List.of());
        }

        return director.toBuilder().id(keyHolder.getKey().intValue()).build();
    }

    @Override
    public List<Director> findAll() {
        String query = """
                SELECT * FROM director
                ORDER BY id;
                """;
        return namedParameterJdbcTemplate.query(query, mapper);
    }

    @Override
    public List<Director> findByFilm(Integer filmId) {
        MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
        String query = """
                SELECT d.* FROM director d
                JOIN film_director fd ON d.id = fd.director_id
                WHERE fd.film_id = :filmId;
                """;
        return namedParameterJdbcTemplate.query(query, params, mapper);
    }

    @Override
    public List<Director> findByIdList(List<Integer> directorIdList) {
        SqlParameterSource params = new MapSqlParameterSource("ids", directorIdList);
        String query = """
                SELECT * FROM director
                WHERE id IN (:ids);
                """;
        return namedParameterJdbcTemplate.query(query, params, mapper);
    }

    @Override
    public List<DirectorBatchDto> findByFilmIdList(List<Integer> filmIdList) {
        SqlParameterSource params = new MapSqlParameterSource("filmIds", filmIdList);
        String query = """
                    SELECT
                        fd.film_id,
                        d.id AS director_id,
                        d.name AS director_name
                    FROM film_director fd
                    JOIN director d ON d.id = fd.director_id
                    WHERE fd.film_id IN (:filmIds)
                    ORDER BY fd.film_id;
                """;
        return namedParameterJdbcTemplate.query(query, params, batchDirectorMapper);
    }

    @Override
    public Director findById(Integer directorId) {
        MapSqlParameterSource params = new MapSqlParameterSource("directorId", directorId);
        String query = """
                SELECT * FROM director
                WHERE id = :directorId;
                """;
        List<Director> result = namedParameterJdbcTemplate.query(query, params, mapper);
        if (result.isEmpty()) {
            LoggedException.throwNew(ExceptionType.DIRECTOR_NOT_FOUND, getClass(), List.of(directorId));
        }
        return result.getFirst();
    }

    @Override
    public Director update(Director director) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("directorName", director.getName())
                .addValue("directorId", director.getId());
        String query = """
                UPDATE director
                SET name = :directorName
                WHERE id = :directorId;
                """;
        int updatedRows = namedParameterJdbcTemplate.update(query,params);
        if (updatedRows == 0) {
            LoggedException.throwNew(ExceptionType.DIRECTOR_NOT_FOUND, getClass(), List.of(director.getId()));
        }
        return director;
    }

    @Override
    public void linkDirectorsToFilm(Integer filmId, List<Integer> directorIds, boolean clearExisting) {
        if (directorIds.isEmpty() || clearExisting) {
            MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
            String deleteDirectorsOfFilmQuery = """
                    DELETE FROM film_director
                    WHERE film_id = :filmId;
                    """;
            namedParameterJdbcTemplate.update(deleteDirectorsOfFilmQuery, params);
        }

        SqlParameterSource[] batchParams = directorIds.stream()
                .map(directorId ->  new MapSqlParameterSource()
                        .addValue("filmId", filmId)
                        .addValue("directorId", directorId))
                .toArray(SqlParameterSource[]::new);
        String insertQuery = """
                INSERT INTO film_director(film_id, director_id)
                VALUES (:filmId, :directorId)
                """;
        namedParameterJdbcTemplate.batchUpdate(insertQuery, batchParams);
    }

    @Override
    public void delete(Integer directorId) {
        MapSqlParameterSource params = new MapSqlParameterSource("directorId", directorId);
        String query = """
                DELETE FROM director
                WHERE id = :directorId;
                """;
        int deletedRows = namedParameterJdbcTemplate.update(query, params);
        if (deletedRows == 0) {
            LoggedException.throwNew(ExceptionType.DIRECTOR_NOT_FOUND, getClass(), List.of(directorId));
        }
    }

    @Component
    private static class DirectorRowMapper implements RowMapper<Director> {
        @Override
        public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Director.builder().id(rs.getInt("id")).name(rs.getString("name")).build();
        }
    }

    @Component
    private static class BatchGenreRowMapper implements RowMapper<DirectorBatchDto> {
        @Override
        public DirectorBatchDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return DirectorBatchDto.builder()
                    .filmId(rs.getInt("film_id"))
                    .directorId(rs.getInt("director_id"))
                    .directorName(rs.getString("director_name"))
                    .build();
        }
    }

    @Builder
    public record DirectorBatchDto(Integer filmId, Integer directorId, String directorName) {
    }
}