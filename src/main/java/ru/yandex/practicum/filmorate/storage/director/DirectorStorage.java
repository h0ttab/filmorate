package ru.yandex.practicum.filmorate.storage.director;

import java.util.List;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage.DirectorBatchDto;

public interface DirectorStorage {
    Director create(Director director);

    List<Director> findAll();

    List<Director> findByFilm(Integer filmId);

    List<Director> findByIdList(List<Integer> idList);

    List<DirectorBatchDto> findByFilmIdList(List<Integer> filmIdList);

    Director findById(Integer directorId);

    Director update(Director director);

    void linkDirectorsToFilm(Integer filmId, List<Integer> directorIds, boolean clearExisting);

    void delete(Integer directorId);
}
