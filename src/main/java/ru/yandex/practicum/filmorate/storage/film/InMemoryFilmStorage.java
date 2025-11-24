package ru.yandex.practicum.filmorate.storage.film;

import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.AbstractInMemoryStorage;
import ru.yandex.practicum.filmorate.util.Validators;

@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage extends AbstractInMemoryStorage<Film> implements FilmStorage {
    private final Validators validators;

    public Map<Integer, Film> getStorage() {
        return Map.copyOf(mapEntityStorage);
    }

    @Override
    public List<Film> findAll() {
        return mapEntityStorage.values().stream().toList();
    }

    @Override
    public Film findById(Integer filmId) {
        validators.validateFilmExists(filmId, getClass());
        return mapEntityStorage.get(filmId);
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        mapEntityStorage.put(film.getId(), film);
        log.info("Добавлен новый фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film filmUpdate) {
        getStorage().put(filmUpdate.getId(), filmUpdate);
        log.info("Обновлён фильм id {}. Новое значение: {}", filmUpdate.getId(), filmUpdate);
        return filmUpdate;
    }

    @Override
    public Integer delete(Integer filmId) {
        mapEntityStorage.remove(filmId);
        log.info("Удалён фильм id {}", filmId);
        return filmId;
    }

    @Override
    public List<Film> findTopLiked(int count) {
        return mapEntityStorage.values().stream()
                .sorted(Comparator.<Film, Integer>comparing(film -> film.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    @Override
    public List<Film> findTopLiked(int count, Integer genreId, Integer year) {
        return mapEntityStorage.values().stream()
                .filter(film -> {
                    // Фильтрация по жанру, если указан genreId
                    if (genreId != null && (film.getGenres() == null ||
                            film.getGenres().stream().noneMatch(genre -> genre.getId().equals(genreId)))) {
                        return false;
                    }

                    // Фильтрация по году, если указан year
                    return year == null || film.getReleaseDate().getYear() == year;
                })
                .sorted(Comparator.<Film, Integer>comparing(film -> film.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    @Override
    public List<Film> findByDirector(Integer directorId, SortOrder order) {
        return List.of();
    }

    @Override
    public List<Film> findCommonFilms(Integer userId, Integer friendId) {
        return mapEntityStorage.values().stream()
                .filter(film -> film.getLikes().contains(userId) && film.getLikes().contains(friendId))
                .sorted(Comparator.<Film, Integer>comparing(film -> film.getLikes().size()).reversed())
                .toList();
    }
}
