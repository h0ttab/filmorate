package com.app.filmorate.controller;

import java.util.Collection;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.app.filmorate.model.Mpa;
import com.app.filmorate.service.MpaService;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    @Autowired
    private final MpaService mpaService;

    @GetMapping("/{id}")
    public Mpa findById(@PathVariable Integer id) {
        return mpaService.findById(id);
    }

    @GetMapping
    public Collection<Mpa> findAll() {
        return mpaService.findAll();
    }
}
