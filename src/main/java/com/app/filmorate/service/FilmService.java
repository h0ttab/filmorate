package com.app.filmorate.service;

import java.util.*;
import java.util.stream.Collectors;

import com.app.filmorate.exception.ExceptionType;
import com.app.filmorate.exception.LoggedException;
import com.app.filmorate.mapper.FilmMapper;
import com.app.filmorate.model.*;
import com.app.filmorate.model.dto.film.FilmCreateDto;
import com.app.filmorate.model.dto.film.FilmUpdateDto;
import com.app.filmorate.storage.film.FilmStorage;
import com.app.filmorate.storage.film.SortOrder;
import com.app.filmorate.util.DtoHelper;
import com.app.filmorate.util.Validators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final FilmMapper filmMapper;
    private final LikeService likeService;
    private final GenreService genreService;
    private final DirectorService directorService;
    private final MpaService mpaService;
    private final DtoHelper dtoHelper;
    private final Validators validators;

    public Collection<Film> findAll() {
        List<Film> films = filmStorage.findAll();
        return addAttributes(films);
    }

    public Film findById(Integer filmId) {
        Film film = filmStorage.findById(filmId);
        return addAttributes(film);
    }

    /**
     * Находит топ N фильмов по количеству лайков с возможностью фильтрации по жанру и году выпуска
     *
     * @param count   количество фильмов для вывода
     * @param genreId идентификатор жанра для фильтрации (может быть null)
     * @param year    год выпуска для фильтрации (может быть null)
     * @return список фильмов, отсортированных по количеству лайков (по убыванию)
     */
    public List<Film> findTopLiked(int count, Integer genreId, Integer year) {
        List<Film> films;

        if (genreId != null) {
            validators.validateGenreExists(genreId, getClass());
        }

        if (genreId == null && year == null) {
            films = filmStorage.findTopLiked(count);
            return addAttributes(films);
        }

        films = filmStorage.findTopLiked(count, genreId, year);
        return addAttributes(films);
    }

    public List<Film> findTopLiked(int count) {
        List<Film> films = filmStorage.findTopLiked(count);
        return addAttributes(films);
    }

    public List<Film> findByDirector(Integer directorId, String sortOrder) {
        try {
            SortOrder order = SortOrder.valueOf(sortOrder.toUpperCase());
            validators.validateDirectorExists(directorId, getClass());
            List<Film> films = filmStorage.findByDirector(directorId, order);
            return addAttributes(films);
        } catch (IllegalArgumentException e) {
            LoggedException.throwNew(ExceptionType.INVALID_SORT_ORDER, getClass(), List.of());
        }
        return List.of();
    }

    public List<Film> findCommonFilms(Integer userId, Integer friendId) {
        validators.validateUserExists(userId, getClass());
        validators.validateUserExists(friendId, getClass());
        List<Film> commonFilms = filmStorage.findCommonFilms(userId, friendId);
        return addAttributes(commonFilms);
    }

    public Film create(FilmCreateDto filmCreateDto) {
        validators.validateCreateFilmDto(filmCreateDto);
        Film film = filmMapper.toEntity(filmCreateDto);
        Film createdFilm = filmStorage.create(film);
        linkAttributesToFilm(film);
        return addAttributes(createdFilm);
    }

    public Film update(FilmUpdateDto filmUpdateDto) {
        validators.validateUpdateFilmDto(filmUpdateDto);
        Film filmUpdate = filmMapper.toEntity(filmUpdateDto);
        Film filmOriginal = findById(filmUpdate.getId());

        filmUpdate.getDirectors()
                .forEach(director -> validators.validateDirectorExists(director.getId(), getClass()));
        validators.validateFilmReleaseDate(filmUpdateDto.getReleaseDate(), getClass());
        validators.validateFilmDescription(filmUpdateDto.getDescription(), filmUpdateDto.getId(), getClass());

        filmUpdate = (Film) dtoHelper.transferFields(filmOriginal, filmUpdate);
        Film updatedFilm = filmStorage.update(filmUpdate);
        linkAttributesToFilm(filmUpdate);
        return addAttributes(updatedFilm);
    }

    public void addLike(Integer filmId, Integer userId) {
        validators.validateFilmExists(filmId, getClass());
        validators.validateUserExists(userId, getClass());
        likeService.addLike(filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        validators.validateFilmExists(filmId, getClass());
        validators.validateUserExists(userId, getClass());
        likeService.removeLike(filmId, userId);
    }

    public void delete(Integer filmId) {
        filmStorage.delete(filmId);
    }

    List<Film> addAttributes(List<Film> films) {
        Map<Integer, Mpa> filmMpaMap;
        Map<Integer, List<Genre>> filmGenreMap;
        Map<Integer, List<Director>> filmDirectorMap;
        Map<Integer, List<Integer>> filmLikeMap;

        List<Integer> filmIdList = films.stream().map(Film::getId).toList();

        filmMpaMap = mpaService.findByFilmIdList(filmIdList);
        filmGenreMap = genreService.findByFilmIdList(filmIdList);
        filmDirectorMap = directorService.findByFilmIdList(filmIdList);
        filmLikeMap = likeService.getLikesByFilmIdList(filmIdList);

        films.forEach(film -> {
            Integer filmId = film.getId();
            Mpa filmMpa = filmMpaMap.get(filmId);
            List<Genre> filmGenres = filmGenreMap.containsKey(filmId) ? filmGenreMap.get(filmId) : List.of();
            List<Director> filmDirectors = filmDirectorMap.containsKey(filmId) ? filmDirectorMap.get(filmId) : List.of();
            List<Integer> filmLikes = filmLikeMap.containsKey(filmId) ? filmLikeMap.get(filmId) : List.of();

            film.setMpa(filmMpa);
            film.setDirectors(filmDirectors);
            film.setGenres(filmGenres);
            film.getLikes().addAll(filmLikes);
        });

        return films;
    }

    private Film addAttributes(Film film) {
        Integer filmId = film.getId();

        Mpa mpa = mpaService.findByFilmId(filmId);
        List<Genre> genres = genreService.findByFilmId(filmId);
        List<Director> directors = directorService.findByFilmId(filmId);
        List<Integer> likes = likeService.getLikesByFilmId(filmId);

        film.getLikes().addAll(likes);
        film.setGenres(genres);
        film.setDirectors(directors);
        film.setMpa(mpa);
        return film;
    }

    private void linkAttributesToFilm(Film film) {
        if (film.getDirectors() != null) {
            List<Integer> directors = film.getDirectors().stream()
                    .mapToInt(Director::getId)
                    .boxed()
                    .toList();
            directorService.linkDirectorToFilm(film.getId(), directors, true);
        }

        if (film.getGenres() != null) {
            Set<Integer> genreIdSet = extractGenreIdSet(film);
            genreService.linkGenresToFilm(film.getId(), genreIdSet, true);
        }
        List<Integer> likes = likeService.getLikesByFilmId(film.getId());
        film.getLikes().addAll(likes);
    }

    private Set<Integer> extractGenreIdSet(Film film) {
        return film.getGenres().stream()
                .mapToInt(Genre::getId)
                .boxed()
                .collect(Collectors.toSet());
    }
}