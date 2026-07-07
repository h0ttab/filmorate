package com.app.filmorate.controller;

import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.app.filmorate.model.Film;
import com.app.filmorate.service.SearchService;

@RestController
@RequestMapping("/films/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public List<Film> searchFilms(@RequestParam String query,
                                  @RequestParam(required = false, defaultValue = "title, director") Set<String> by) {
        return searchService.searchFilms(query, by);
    }
}