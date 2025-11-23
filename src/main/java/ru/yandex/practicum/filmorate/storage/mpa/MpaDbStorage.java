package ru.yandex.practicum.filmorate.storage.mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<Mpa> mapper;
    private final RowMapper<MpaBatchDto> mpaBatchRowMapper;

    @Override
    public List<Mpa> findAll() {
        String query = """
                SELECT * FROM mpa
                ORDER BY id;
                """;
        return namedParameterJdbcTemplate.query(query, mapper);
    }

    @Override
    public Mpa findById(Integer mpaId) {
        String query = """
                SELECT * FROM mpa
                WHERE id = :id;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("id", mpaId);
        return namedParameterJdbcTemplate.queryForObject(query, params, mapper);
    }

    @Override
    public Mpa findByFilmId(Integer filmId) {
        String query = """
                SELECT * FROM mpa
                JOIN film f on f.mpa_id = mpa.id
                WHERE f.id = :filmId;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
        return namedParameterJdbcTemplate.queryForObject(query, params, mapper);
    }

    @Override
    public List<Mpa> findByIdSet(Set<Integer> idList) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("mpaIdList", idList);
        String query = """
                SELECT * FROM mpa
                WHERE id in (:mpaIdList);
                """;
        return namedParameterJdbcTemplate.query(query, parameterSource, mapper);
    }

    @Override
    public List<MpaBatchDto> findByFilmIdList(List<Integer> filmIdList) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("filmIds", filmIdList);
        String query = """
                SELECT
                    f.id AS film_id, m.id AS mpa_id, m.name AS mpa_name
                FROM mpa m
                JOIN film f on f.mpa_id = m.id
                WHERE f.id in (:filmIds);
                """;
        return namedParameterJdbcTemplate.query(query, parameterSource, mpaBatchRowMapper);
    }

    @Component
    private static class MpaRowMapper implements RowMapper<Mpa> {
        @Override
        public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Mpa.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .build();
        }
    }

    @Component
    private static class MpaBatchRowMapper implements RowMapper<MpaBatchDto> {
        @Override
        public MpaBatchDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return MpaBatchDto.builder()
                    .filmId(rs.getInt("film_id"))
                    .mpaId(rs.getInt("mpa_id"))
                    .mpaName(rs.getString("mpa_name"))
                    .build();
        }
    }

    @Builder
    public record MpaBatchDto(Integer filmId, Integer mpaId, String mpaName) {
    }
}