package ru.yandex.practicum.filmorate.storage.mpa;

import java.util.List;
import java.util.Set;

import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage.MpaBatchDto;

public interface MpaStorage {
    List<Mpa> findAll();

    Mpa findById(Integer mpaId);

    Mpa findByFilmId(Integer filmId);

    List<Mpa> findByIdSet(Set<Integer> idList);

    List<MpaBatchDto> findByFilmIdList(List<Integer> filmIdList);
}