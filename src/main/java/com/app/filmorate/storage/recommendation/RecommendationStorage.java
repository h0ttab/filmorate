package com.app.filmorate.storage.recommendation;

import java.util.List;

import com.app.filmorate.model.Film;

/**
 * Интерфейс для хранилища рекомендаций фильмов.
 * Предоставляет методы для работы с рекомендациями фильмов.
 */
public interface RecommendationStorage {

    /**
     * Получает список фильмов, которым поставил лайк указанный пользователь.
     *
     * @param userId идентификатор пользователя
     * @return список идентификаторов фильмов, которым поставил лайк пользователь
     */
    List<Integer> getLikedFilmsByUserId(Integer userId);


    /**
     * Получает список рекомендованных фильмов для указанного пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список рекомендованных фильмов
     */
    List<Film> getRecommendations(Integer userId);
}