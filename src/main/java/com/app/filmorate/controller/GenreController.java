package com.app.filmorate.controller;

import java.util.Collection;

import com.app.filmorate.model.Genre;
import com.app.filmorate.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    @Autowired
    private final GenreService genreService;

    @GetMapping
    public Collection<Genre> findAll() {
        return genreService.findAll();
    }

    @GetMapping("/{id}")
    public Genre findById(@PathVariable Integer id) {
        return genreService.findById(id);
    }
}
