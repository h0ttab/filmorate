package com.app.filmorate.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.app.filmorate.model.Director;
import com.app.filmorate.model.dto.film.DirectorDto;
import com.app.filmorate.service.DirectorService;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> findAll() {
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director findById(@PathVariable Integer id) {
        return directorService.findById(id);
    }

    @PostMapping
    public Director create(@Valid @RequestBody DirectorDto directorDto) {
        return directorService.create(directorDto);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        directorService.delete(id);
    }
}
