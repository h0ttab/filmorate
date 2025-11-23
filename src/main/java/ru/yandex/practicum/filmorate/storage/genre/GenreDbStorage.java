package ru.yandex.practicum.filmorate.storage.genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<Genre> mapper;
    private final RowMapper<GenreBatchDto> batchGenreMapper;

    @Override
    public List<Genre> findAll() {
        String query = """
                SELECT * FROM genre
                ORDER BY id;
                """;
        return namedParameterJdbcTemplate.query(query, mapper);
    }

    @Override
    public Genre findById(Integer genreId) {
        String query = """
                SELECT * FROM genre
                WHERE id = :id;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("id", genreId);
        return namedParameterJdbcTemplate.queryForObject(query, params, mapper);
    }

    @Override
    public List<Genre> findByFilmId(Integer filmId) {
        String query = """
                    SELECT g.*
                    FROM genre g
                    JOIN film_genre fg ON g.id = fg.genre_id
                    WHERE fg.film_id = :filmId
                    ORDER BY fg.genre_id;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
        return namedParameterJdbcTemplate.query(query, params, mapper);
    }

    @Override
    public List<Genre> findByIdList(List<Integer> genreIdList) {
        SqlParameterSource parameters = new MapSqlParameterSource("ids", genreIdList);
        String query = """
                    SELECT * FROM genre
                    WHERE id IN (:ids)
                """;
        return namedParameterJdbcTemplate.query(query, parameters, mapper);
    }

    @Override
    public List<GenreBatchDto> findByFilmIdList(List<Integer> filmIdList) {
        SqlParameterSource parameters = new MapSqlParameterSource("filmIds", filmIdList);
        String query = """
                    SELECT
                        fg.film_id,
                        g.id AS genre_id,
                        g.name AS genre_name
                    FROM film_genre fg
                    JOIN genre g ON g.id = fg.genre_id
                    WHERE fg.film_id IN (:filmIds)
                    ORDER BY fg.film_id;
                """;
        return namedParameterJdbcTemplate.query(query, parameters, batchGenreMapper);
    }

    @Override
    public void linkGenresToFilm(Integer filmId, Set<Integer> genreIdSet, boolean clearExisting) {
        if (clearExisting) {
            String deleteGenresOfFilmQuery = "DELETE FROM film_genre WHERE film_id = :filmId;";
            MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
            namedParameterJdbcTemplate.update(deleteGenresOfFilmQuery, params);
        }

        if (genreIdSet == null || genreIdSet.isEmpty()) {
            return;
        }

        String insertQuery = "INSERT INTO film_genre (film_id, genre_id) VALUES (:filmId, :genreId);";

        SqlParameterSource[] batch = genreIdSet.stream()
                .map(genreId -> new MapSqlParameterSource()
                        .addValue("filmId", filmId)
                        .addValue("genreId", genreId))
                .toArray(SqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(insertQuery, batch);
    }

    @Component
    private static class GenreRowMapper implements RowMapper<Genre> {
        @Override
        public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Genre.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .build();
        }
    }

    @Component
    private static class BatchGenreRowMapper implements RowMapper<GenreBatchDto> {
        @Override
        public GenreBatchDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return GenreBatchDto.builder()
                    .filmId(rs.getInt("film_id"))
                    .genreId(rs.getInt("genre_id"))
                    .genreName(rs.getString("genre_name"))
                    .build();
        }
    }

    @Builder
    public record GenreBatchDto(Integer filmId, Integer genreId, String genreName) {
    }
}