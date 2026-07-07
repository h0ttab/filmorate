package com.app.filmorate.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.app.filmorate.exception.ExceptionType;
import com.app.filmorate.exception.LoggedException;
import com.app.filmorate.model.Film;
import com.app.filmorate.model.search.SearchTarget;
import com.app.filmorate.storage.search.Search;
import com.app.filmorate.util.Validators;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final Search search;
    private final Validators validators;
    private final FilmService filmService;

    public List<Film> searchFilms(String searchQuery, Set<String> searchRequestTargetParams) {
        if (!validators.isValidString(searchQuery)) {
            LoggedException.throwNew(ExceptionType.INVALID_SEARCH_REQUEST, getClass(), List.of());
        }
        Set<SearchTarget> searchTargetSet;
        try {
            searchTargetSet = searchRequestTargetParams.stream()
                    .map(st -> SearchTarget.valueOf(st.toUpperCase()))
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            LoggedException.throwNew(ExceptionType.INVALID_SEARCH_REQUEST, getClass(), List.of());
            return List.of();
        }
        List<Film> films = search.searchFilms(searchQuery, searchTargetSet);
        return filmService.addAttributes(films);
    }
}