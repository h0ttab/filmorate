package ru.yandex.practicum.filmorate.storage.search;

import java.util.List;
import java.util.Set;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.search.SearchTarget;

public interface Search {
    List<Film> searchFilms(String searchQuery, Set<SearchTarget> searchTargetSet);
}