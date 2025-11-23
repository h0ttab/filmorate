package ru.yandex.practicum.filmorate.storage.director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(DirectorSqlQueries.CREATE.getQuery(), params, keyHolder, new String[]{"id"});

        if (Optional.ofNullable(keyHolder.getKey()).isEmpty()) {
            LoggedException.throwNew(ExceptionType.UNEXPECTED_ERROR, getClass(), List.of());
        }

        return director.toBuilder().id(keyHolder.getKey().intValue()).build();
    }

    @Override
    public List<Director> findAll() {
        return namedParameterJdbcTemplate.query(DirectorSqlQueries.FIND_ALL.getQuery(), mapper);
    }

    @Override
    public List<Director> findByFilm(Integer filmId) {
        MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
        return namedParameterJdbcTemplate.query(DirectorSqlQueries.FIND_BY_FILM_ID.getQuery(), params, mapper);
    }

    @Override
    public List<Director> findByIdList(List<Integer> directorIdList) {
        SqlParameterSource params = new MapSqlParameterSource("ids", directorIdList);
        return namedParameterJdbcTemplate.query(DirectorSqlQueries.FIND_BY_ID_LIST.getQuery(), params, mapper);
    }

    @Override
    public List<DirectorBatchDto> findByFilmIdList(List<Integer> filmIdList) {
        SqlParameterSource params = new MapSqlParameterSource("filmIds", filmIdList);
        return namedParameterJdbcTemplate.query(DirectorSqlQueries.FIND_BY_FILM_ID_LIST.getQuery(),
                params, batchDirectorMapper);
    }

    @Override
    public Director findById(Integer directorId) {
        MapSqlParameterSource params = new MapSqlParameterSource("directorId", directorId);
        List<Director> result = namedParameterJdbcTemplate.query(DirectorSqlQueries.FIND_BY_ID.getQuery(),
                params, mapper);
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
        int updatedRows = namedParameterJdbcTemplate.update(DirectorSqlQueries.UPDATE.getQuery(), params);
        if (updatedRows == 0) {
            LoggedException.throwNew(ExceptionType.DIRECTOR_NOT_FOUND, getClass(), List.of(director.getId()));
        }
        return director;
    }

    @Override
    public void linkDirectorsToFilm(Integer filmId, List<Integer> directorIds, boolean clearExisting) {
        if (directorIds.isEmpty() || clearExisting) {
            MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
            namedParameterJdbcTemplate.update(DirectorSqlQueries.LINK_DELETE.getQuery(), params);
        }

        SqlParameterSource[] batchParams = directorIds.stream()
                .map(directorId -> new MapSqlParameterSource()
                        .addValue("filmId", filmId)
                        .addValue("directorId", directorId))
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(DirectorSqlQueries.LINK_INSERT.getQuery(), batchParams);
    }

    @Override
    public void delete(Integer directorId) {
        MapSqlParameterSource params = new MapSqlParameterSource("directorId", directorId);
        int deletedRows = namedParameterJdbcTemplate.update(DirectorSqlQueries.DELETE.getQuery(), params);
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