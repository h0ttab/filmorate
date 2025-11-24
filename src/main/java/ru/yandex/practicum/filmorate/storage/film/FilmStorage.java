package ru.yandex.practicum.filmorate.storage.film;

import java.util.List;

import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage {
    List<Film> findAll();

    Film findById(Integer filmId);

    Film create(Film film);

    Film update(Film filmUpdate);

    Integer delete(Integer filmId);

    List<Film> findTopLiked(int size);

    /**
     * Находит топ N фильмов по количеству лайков с возможностью фильтрации по жанру и году выпуска
     *
     * @param count   количество фильмов для вывода
     * @param genreId идентификатор жанра для фильтрации (может быть null)
     * @param year    год выпуска для фильтрации (может быть null)
     * @return список фильмов, отсортированных по количеству лайков (по убыванию)
     */
    List<Film> findTopLiked(int count, Integer genreId, Integer year);

    List<Film> findByDirector(Integer directorId, SortOrder order);

    /**
     * Находит фильмы, которые лайкнули оба пользователя, отсортированные
     * по популярности (общему количеству лайков).
     *
     * @param userId   идентификатор пользователя
     * @param friendId идентификатор друга
     * @return список общих фильмов
     */
    List<Film> findCommonFilms(Integer userId, Integer friendId);
}
