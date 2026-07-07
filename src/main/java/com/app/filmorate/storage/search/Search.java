package com.app.filmorate.storage.search;

import java.util.List;
import java.util.Set;

import com.app.filmorate.model.Film;
import com.app.filmorate.model.search.SearchTarget;

public interface Search {
    List<Film> searchFilms(String searchQuery, Set<SearchTarget> searchTargetSet);
}