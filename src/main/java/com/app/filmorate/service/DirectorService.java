package com.app.filmorate.service;

import java.util.*;

import com.app.filmorate.model.Director;
import com.app.filmorate.model.dto.film.DirectorDto;
import com.app.filmorate.storage.director.DirectorDbStorage.DirectorBatchDto;
import com.app.filmorate.storage.director.DirectorStorage;
import com.app.filmorate.util.Validators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;
    private final Validators validators;

    public Director create(DirectorDto directorDto) {
        Director director = Director.builder().name(directorDto.getName()).build();
        Director createdDirector = directorStorage.create(director);
        log.info("Добавлен режиссёр {}", createdDirector);
        return createdDirector;
    }

    public List<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director findById(Integer directorId) {
        validators.validateDirectorExists(directorId, getClass());
        return directorStorage.findById(directorId);
    }

    public List<Director> findByFilmId(Integer filmId) {
        validators.validateFilmExists(filmId, getClass());
        return directorStorage.findByFilm(filmId);
    }

    public List<Director> findByIdList(List<Integer> directorIdList) {
        return directorStorage.findByIdList(directorIdList);
    }

    public Map<Integer, List<Director>> findByFilmIdList(List<Integer> filmIdList) {
        List<DirectorBatchDto> directorBatchDtoList = directorStorage.findByFilmIdList(filmIdList);
        Map<Integer, List<Director>> filmDirectorMap = new HashMap<>();
        directorBatchDtoList.forEach(directorBatchDto -> {
            filmDirectorMap.computeIfAbsent(directorBatchDto.filmId(), v -> new ArrayList<>())
                    .add(Director.builder().id(directorBatchDto.directorId()).name(directorBatchDto.directorName()).build());
        });
        return filmDirectorMap;
    }

    public void linkDirectorToFilm(Integer filmId, List<Integer> directorIds, boolean clearExisting) {
        directorStorage.linkDirectorsToFilm(filmId, directorIds, clearExisting);
    }

    public Director update(Director director) {
        Director updatedDirector = directorStorage.update(director);
        log.info("Обновлена информация о режиссёре id={}. Новое значение: {}", updatedDirector.getId(), updatedDirector);
        return updatedDirector;
    }

    public void delete(Integer directorId) {
        directorStorage.delete(directorId);
        log.info("Удален режиссёр id={}", directorId);
    }
}
