package ru.yandex.practicum.filmorate.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.recommendation.RecommendationStorage;
import ru.yandex.practicum.filmorate.util.Validators;

/**
 * Сервис для работы с рекомендациями фильмов.
 * Предоставляет методы для получения рекомендаций фильмов для пользователя.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationStorage recommendationStorage;
    private final Validators validators;
    private final FilmService filmService;

    /**
     * Получает список рекомендованных фильмов для указанного пользователя.
     * Рекомендации формируются на основе алгоритма коллаборативной фильтрации:
     * 1. Находим пользователей с максимальным количеством пересечения по лайкам.
     * 2. Определяем фильмы, которые один пролайкал, а другой нет.
     * 3. Рекомендуем фильмы, которым поставил лайк пользователь с похожими вкусами,
     * а тот, для кого составляется рекомендация, ещё не поставил.
     *
     * @param userId идентификатор пользователя
     * @return список рекомендованных фильмов
     */
    public List<Film> getRecommendations(Integer userId) {
        validators.validateUserExists(userId, getClass());

        log.info("Запрос на получение рекомендаций для пользователя с id {}", userId);
        List<Film> recommendations = recommendationStorage.getRecommendations(userId);
        recommendations = filmService.addAttributes(recommendations);
        log.info("Получены рекомендации для пользователя с id {}: {} фильмов", userId, recommendations.size());

        return recommendations;
    }
}