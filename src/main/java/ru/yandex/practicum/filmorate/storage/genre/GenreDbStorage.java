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
        return namedParameterJdbcTemplate.query(GenreSqlQueries.FIND_ALL.getQuery(), mapper);
    }

    @Override
    public Genre findById(Integer genreId) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", genreId);
        return namedParameterJdbcTemplate.queryForObject(GenreSqlQueries.FIND_BY_ID.getQuery(), params, mapper);
    }

    @Override
    public List<Genre> findByFilmId(Integer filmId) {
        MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
        return namedParameterJdbcTemplate.query(GenreSqlQueries.FIND_BY_FILM_ID.getQuery(), params, mapper);
    }

    @Override
    public List<Genre> findByIdList(List<Integer> genreIdList) {
        SqlParameterSource parameters = new MapSqlParameterSource("ids", genreIdList);
        return namedParameterJdbcTemplate.query(GenreSqlQueries.FIND_BY_ID_LIST.getQuery(), parameters, mapper);
    }

    @Override
    public List<GenreBatchDto> findByFilmIdList(List<Integer> filmIdList) {
        SqlParameterSource parameters = new MapSqlParameterSource("filmIds", filmIdList);
        return namedParameterJdbcTemplate.query(GenreSqlQueries.FIND_BY_FILM_ID_LIST.getQuery(),
                parameters, batchGenreMapper);
    }

    @Override
    public void linkGenresToFilm(Integer filmId, Set<Integer> genreIdSet, boolean clearExisting) {
        if (clearExisting) {
            MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
            namedParameterJdbcTemplate.update(GenreSqlQueries.LINK_DELETE.getQuery(), params);
        }

        if (genreIdSet == null || genreIdSet.isEmpty()) {
            return;
        }

        SqlParameterSource[] batch = genreIdSet.stream()
                .map(genreId -> new MapSqlParameterSource()
                        .addValue("filmId", filmId)
                        .addValue("genreId", genreId))
                .toArray(SqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(GenreSqlQueries.LINK_INSERT.getQuery(), batch);
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