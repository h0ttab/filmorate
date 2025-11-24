package ru.yandex.practicum.filmorate.storage.recommendation;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

/**
 * Реализация хранилища рекомендаций фильмов с использованием базы данных.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationDbStorage implements RecommendationStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmDbStorage.FilmRowMapper filmRowMapper;

    /**
     * Получает список идентификаторов фильмов, которым поставил лайк указанный пользователь.
     *
     * @param userId идентификатор пользователя
     * @return список идентификаторов фильмов, которым поставил лайк пользователь
     */
    @Override
    public List<Integer> getLikedFilmsByUserId(Integer userId) {
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        return namedParameterJdbcTemplate.queryForList(RecommendationSqlQueries.GET_LIKED_FILMS_BY_USER_ID.getQuery(), params, Integer.class);
    }


    /**
     * Получает список рекомендованных фильмов для указанного пользователя.
     * Метод использует SQL-запрос для получения рекомендаций напрямую из базы данных.
     *
     * @param userId идентификатор пользователя
     * @return список рекомендованных фильмов
     */
    @Override
    public List<Film> getRecommendations(Integer userId) {
        // Получаем список фильмов, которым поставил лайк пользователь
        List<Integer> userLikedFilms = getLikedFilmsByUserId(userId);

        if (userLikedFilms.isEmpty()) {
            log.info("Пользователь с id {} не поставил ни одного лайка, рекомендации не могут быть сформированы", userId);
            return List.of();
        }

        // SQL-запрос для получения рекомендаций
        // Находим пользователей с максимальным пересечением по лайкам
        // и рекомендуем фильмы, которые они лайкнули, а текущий пользователь - нет
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);

        List<Film> rawRecommendations = namedParameterJdbcTemplate.query(RecommendationSqlQueries.GET_RECOMMENDATIONS.getQuery(), params, filmRowMapper);

        Set<Integer> likedFilmIds = new HashSet<>(userLikedFilms);
        List<Film> recommendations = rawRecommendations.stream()
                .filter(film -> !likedFilmIds.contains(film.getId()))
                .toList();

        log.info("Сформированы рекомендации для пользователя с id {}: {} фильмов", userId, recommendations.size());
        return recommendations;
    }
}