package ru.yandex.practicum.filmorate.storage.director;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<Director> mapper;
    private final RowMapper<DirectorBatchDto> batchDirectorMapper;


    @Override
    public Director create(Director director) {
        String query = """
                INSERT INTO director (name)
                VALUES (?);
                """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(query, new String[]{"id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

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
        return jdbcTemplate.query(query, mapper);
    }

    @Override
    public List<Director> findByFilm(Integer filmId) {
        String query = """
                SELECT d.* FROM director d
                JOIN film_director fd ON d.id = fd.director_id
                WHERE fd.film_id = ?;
                """;
        return jdbcTemplate.query(query, mapper, filmId);
    }

    @Override
    public List<Director> findByIdList(List<Integer> directorIdList) {
        String query = """
                SELECT * FROM director
                WHERE id IN (:ids);
                """;
        SqlParameterSource parameters = new MapSqlParameterSource("ids", directorIdList);
        return namedParameterJdbcTemplate.query(query, parameters, mapper);
    }

    @Override
    public List<DirectorBatchDto> findByFilmIdList(List<Integer> filmIdList) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("filmIds", filmIdList);
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
        return namedParameterJdbcTemplate.query(query, parameterSource, batchDirectorMapper);
    }

    @Override
    public Director findById(Integer directorId) {
        String query = """
                SELECT * FROM director
                WHERE id = ?;
                """;
        List<Director> result = jdbcTemplate.query(query, mapper, directorId);
        if (result.isEmpty()) {
            LoggedException.throwNew(ExceptionType.DIRECTOR_NOT_FOUND, getClass(), List.of(directorId));
        }
        return result.getFirst();
    }

    @Override
    public Director update(Director director) {
        String query = """
                UPDATE director
                SET name = ?
                WHERE id = ?;
                """;
        int updatedRows = jdbcTemplate.update(query, director.getName(), director.getId());
        if (updatedRows == 0) {
            LoggedException.throwNew(ExceptionType.DIRECTOR_NOT_FOUND, getClass(), List.of(director.getId()));
        }
        return director;
    }

    @Override
    public void linkDirectorsToFilm(Integer filmId, List<Integer> directorIds, boolean clearExisting) {
        if (clearExisting) {
            String deleteDirectorsOfFilmQuery = """
                    DELETE FROM film_director
                    WHERE film_id = ?;
                    """;
            jdbcTemplate.update(deleteDirectorsOfFilmQuery, filmId);
        }

        if (directorIds == null || directorIds.isEmpty()) {
            return;
        }

        String insertQuery = """
                INSERT INTO film_director(film_id, director_id)
                VALUES (?, ?)
                """;

        jdbcTemplate.batchUpdate(insertQuery, directorIds, directorIds.size(),
                (ps, directorId) -> {
                    ps.setInt(1, filmId);
                    ps.setInt(2, directorId);
                });
    }

    @Override
    public void delete(Integer directorId) {
        String query = """
                DELETE FROM director
                WHERE id = ?;
                """;
        int deletedRows = jdbcTemplate.update(query, directorId);
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