package ru.yandex.practicum.filmorate.storage.like;

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
        String query = """
                INSERT INTO "like" (film_id, user_id)
                VALUES (:filmId, :userId);
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);
        namedParameterJdbcTemplate.update(query, params);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        String query = """
                DELETE FROM "like"
                WHERE film_id = :filmId
                AND user_id = :userId;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);
        namedParameterJdbcTemplate.update(query, params);
    }

    @Override
    public List<Integer> getLikesByFilmId(Integer filmId) {
        String query = """
                SELECT user_id FROM "like"
                WHERE film_id = :filmId;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
        return namedParameterJdbcTemplate.queryForList(query, params, Integer.class);
    }

    @Override
    public List<LikeDto> getLikesByFilmIdList(List<Integer> filmIdList) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("filmIds", filmIdList);
        String query = """
                SELECT
                	film_id,
                	user_id
                FROM "like"
                WHERE film_id in (:filmIds)
                ORDER BY film_id;
                """;
        return namedParameterJdbcTemplate.query(query, parameterSource, likeBatchRowMapper);
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