package com.app.filmorate.service;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.app.filmorate.exception.ExceptionType;
import com.app.filmorate.exception.LoggedException;
import com.app.filmorate.model.Genre;
import com.app.filmorate.storage.genre.GenreDbStorage;
import com.app.filmorate.storage.genre.GenreDbStorage.GenreBatchDto;
import com.app.filmorate.util.Validators;

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
