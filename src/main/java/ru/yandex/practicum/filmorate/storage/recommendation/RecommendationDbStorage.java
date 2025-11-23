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
        String query = """
                SELECT film_id FROM "like"
                WHERE user_id = :userId;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        return namedParameterJdbcTemplate.queryForList(query, params, Integer.class);
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
        String query = """
                WITH user_likes AS (
                    SELECT film_id
                    FROM "like"
                    WHERE user_id = :userId
                ),
                similar_users AS (
                    SELECT l.user_id, COUNT(*) AS common_likes
                    FROM "like" l
                    JOIN user_likes ul ON l.film_id = ul.film_id
                    WHERE l.user_id != :userId
                    GROUP BY l.user_id
                ),
                recommendations AS (
                    SELECT
                        l.film_id,
                        MAX(su.common_likes) AS score
                    FROM "like" l
                    JOIN similar_users su ON l.user_id = su.user_id
                    WHERE l.film_id NOT IN (SELECT film_id FROM user_likes)
                    GROUP BY l.film_id
                )
                SELECT f.*
                FROM film f
                JOIN recommendations r ON f.id = r.film_id
                ORDER BY r.score DESC, f.id
                LIMIT 10;
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);

        List<Film> rawRecommendations = namedParameterJdbcTemplate.query(query, params, filmRowMapper);

        Set<Integer> likedFilmIds = new HashSet<>(userLikedFilms);
        List<Film> recommendations = rawRecommendations.stream()
                .filter(film -> !likedFilmIds.contains(film.getId()))
                .toList();

        log.info("Сформированы рекомендации для пользователя с id {}: {} фильмов", userId, recommendations.size());
        return recommendations;
    }
}