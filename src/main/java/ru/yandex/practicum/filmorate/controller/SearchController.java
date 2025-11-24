package ru.yandex.practicum.filmorate.controller;

import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.SearchService;

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