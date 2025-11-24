package ru.yandex.practicum.filmorate.service;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage.MpaBatchDto;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.util.Validators;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;
    private final Validators validators;

    public List<Mpa> findAll() {
        return mpaStorage.findAll();
    }

    public Mpa findById(Integer mpaId) {
        validators.validateMpaExists(mpaId, getClass());
        return mpaStorage.findById(mpaId);
    }

    public Mpa findByFilmId(Integer filmId) {
        validators.validateFilmExists(filmId, getClass());
        return mpaStorage.findByFilmId(filmId);
    }

    public Map<Integer, Mpa> findByFilmIdList(List<Integer> filmIdList) {
        List<MpaBatchDto> mpaBatchDtoList = mpaStorage.findByFilmIdList(filmIdList);
        Map<Integer, Mpa> filmMpaMap = new HashMap<>();
        mpaBatchDtoList.forEach(mpaBatchDto -> {
            Mpa mpa = Mpa.builder().id(mpaBatchDto.mpaId()).name(mpaBatchDto.mpaName()).build();
            filmMpaMap.put(mpaBatchDto.filmId(), mpa);
        });
        return filmMpaMap;
    }

    public List<Mpa> findByIdSet(Set<Integer> idSet) {
        return mpaStorage.findByIdSet(idSet);
    }
}
