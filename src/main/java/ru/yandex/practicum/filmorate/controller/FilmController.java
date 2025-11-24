package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.service.FilmService;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Integer id) {
        return filmService.findById(id);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> findByDirector(@PathVariable Integer directorId, @RequestParam String sortBy) {
        return filmService.findByDirector(directorId, sortBy);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable Integer filmId, @PathVariable Integer userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.removeLike(id, userId);
    }

    /**
     * Возвращает список самых популярных фильмов с возможностью фильтрации по жанру и году выпуска
     *
     * @param count   количество фильмов для вывода (по умолчанию 10)
     * @param genreId идентификатор жанра для фильтрации (опционально)
     * @param year    год выпуска для фильтрации (опционально)
     * @return список фильмов, отсортированных по количеству лайков (по убыванию)
     */
    @GetMapping("/popular")
    public List<Film> findTopLiked(
            @RequestParam(required = false, defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {

        return filmService.findTopLiked(count, genreId, year);
    }

    /**
     * Возвращает список общих с другом фильмов, отсортированных по популярности.
     *
     * @param userId   идентификатор пользователя, запрашивающего информацию
     * @param friendId идентификатор пользователя, с которым сравниваем список фильмов
     */
    @GetMapping("/common")
    public List<Film> findCommonFilms(@RequestParam Integer userId,
                                      @RequestParam Integer friendId) {
        return filmService.findCommonFilms(userId, friendId);
    }

    @PostMapping
    public Film create(@Valid @NotNull @RequestBody FilmCreateDto filmCreateDto) {
        return filmService.create(filmCreateDto);
    }

    @PutMapping
    public Film update(@Valid @RequestBody FilmUpdateDto filmUpdateDto) {
        return filmService.update(filmUpdateDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        filmService.delete(id);
    }
}