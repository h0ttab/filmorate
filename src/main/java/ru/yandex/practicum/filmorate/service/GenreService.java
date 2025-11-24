package ru.yandex.practicum.filmorate.service;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage.GenreBatchDto;
import ru.yandex.practicum.filmorate.util.Validators;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreStorage;
    private final Validators validators;

    public List<Genre> findAll() {
        return genreStorage.findAll();
    }

    public Genre findById(Integer genreId) {
        validators.validateGenreExists(genreId, getClass());
        return genreStorage.findById(genreId);
    }

    public List<Genre> findByFilmId(Integer filmId) {
        return genreStorage.findByFilmId(filmId);
    }

    public List<Genre> findByIdList(List<Integer> idList) {
        if (idList.isEmpty()) {
            return List.of();
        }
        List<Genre> genreList = genreStorage.findByIdList(idList);
        if (genreList.isEmpty()) {
            LoggedException.throwNew(ExceptionType.GENRE_NOT_FOUND, getClass(), idList);
        }
        return genreList;
    }

    public Map<Integer, List<Genre>> findByFilmIdList(List<Integer> filmIdList) {
        List<GenreBatchDto> genreBatchDtoList = genreStorage.findByFilmIdList(filmIdList);
        Map<Integer, List<Genre>> filmGenreMap = new HashMap<>();
        genreBatchDtoList.forEach(genreBatchDto -> {
            filmGenreMap.computeIfAbsent(genreBatchDto.filmId(), v -> new ArrayList<>())
                    .add(Genre.builder().id(genreBatchDto.genreId()).name(genreBatchDto.genreName()).build());
        });
        return filmGenreMap;
    }

    public void linkGenresToFilm(Integer filmId, Set<Integer> genreIdSet, boolean clearExisting) {
        genreStorage.linkGenresToFilm(filmId, genreIdSet, clearExisting);
    }
}
