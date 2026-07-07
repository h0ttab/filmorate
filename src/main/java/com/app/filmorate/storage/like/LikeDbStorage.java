package com.app.filmorate.storage.like;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeDbStorage implements LikeStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final LikeBatchRowMapper likeBatchRowMapper;

    @Override
    public void addLike(Integer filmId, Integer userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);
        namedParameterJdbcTemplate.update(LikeSqlQueries.ADD_LIKE.getQuery(), params);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);
        namedParameterJdbcTemplate.update(LikeSqlQueries.REMOVE_LIKE.getQuery(), params);
    }

    @Override
    public List<Integer> getLikesByFilmId(Integer filmId) {
        MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
        return namedParameterJdbcTemplate.queryForList(LikeSqlQueries.GET_LIKES_BY_FILM_ID.getQuery(),
                params, Integer.class);
    }

    @Override
    public List<LikeDto> getLikesByFilmIdList(List<Integer> filmIdList) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("filmIds", filmIdList);
        return namedParameterJdbcTemplate.query(LikeSqlQueries.GET_LIKES_BY_FILM_ID_LIST.getQuery(),
                parameterSource, likeBatchRowMapper);
    }

    @Builder
    public record LikeDto(Integer filmId, Integer userId) {
    }

    @Component
    private static class LikeBatchRowMapper implements RowMapper<LikeDto> {
        @Override
        public LikeDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return LikeDto.builder()
                    .filmId(rs.getInt("film_id"))
                    .userId(rs.getInt("user_id"))
                    .build();
        }
    }
}