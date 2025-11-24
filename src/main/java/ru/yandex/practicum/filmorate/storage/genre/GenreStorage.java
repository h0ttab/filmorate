package ru.yandex.practicum.filmorate.storage.genre;

import java.util.List;
import java.util.Set;

import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage.GenreBatchDto;

public interface GenreStorage {
    List<Genre> findAll();

    Genre findById(Integer genreId);

    List<Genre> findByFilmId(Integer filmId);

    List<Genre> findByIdList(List<Integer> idList);

    List<GenreBatchDto> findByFilmIdList(List<Integer> filmIdList);

    void linkGenresToFilm(Integer filmId, Set<Integer> genreIdSet, boolean clearExisting);
}
