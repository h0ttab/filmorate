package com.app.filmorate.storage.mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Component;
import com.app.filmorate.model.Mpa;

@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<Mpa> mapper;
    private final RowMapper<MpaBatchDto> mpaBatchRowMapper;

    @Override
    public List<Mpa> findAll() {
        return namedParameterJdbcTemplate.query(MpaSqlQueries.FIND_ALL.getQuery(), mapper);
    }

    @Override
    public Mpa findById(Integer mpaId) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", mpaId);
        return namedParameterJdbcTemplate.queryForObject(MpaSqlQueries.FIND_BY_ID.getQuery(), params, mapper);
    }

    @Override
    public Mpa findByFilmId(Integer filmId) {
        MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
        return namedParameterJdbcTemplate.queryForObject(MpaSqlQueries.FIND_BY_FILM_ID.getQuery(), params, mapper);
    }

    @Override
    public List<Mpa> findByIdSet(Set<Integer> idList) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("mpaIdList", idList);
        return namedParameterJdbcTemplate.query(MpaSqlQueries.FIND_BY_ID_SET.getQuery(), parameterSource, mapper);
    }

    @Override
    public List<MpaBatchDto> findByFilmIdList(List<Integer> filmIdList) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("filmIds", filmIdList);
        return namedParameterJdbcTemplate.query(MpaSqlQueries.FIND_BY_FILM_ID_LIST.getQuery(),
                parameterSource, mpaBatchRowMapper);
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