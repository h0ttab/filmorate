package ru.yandex.practicum.filmorate.mapper;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.dto.ObjectIdDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.service.*;
import ru.yandex.practicum.filmorate.util.Validators;

@Component
@RequiredArgsConstructor
public class FilmMapper {
    private final Validators validators;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;

    public Film toEntity(FilmCreateDto dto) {
        List<Genre> genres = List.of();

        if (Optional.ofNullable(dto.getGenres()).isPresent()) {
            List<Integer> genreIdList = dto.getGenres().stream().mapToInt(ObjectIdDto::getId).boxed().toList();
            genres = genreService.findByIdList(genreIdList);
        }

        if (Optional.ofNullable(dto.getDirectors()).isPresent()) {
            dto.getDirectors().stream().mapToInt(ObjectIdDto::getId).boxed().forEach(id -> validators.validateDirectorExists(id, getClass()));
        }
        Mpa mpa = mpaService.findById(dto.getMpa().getId());

        List<Director> directors;
        if (Optional.ofNullable(dto.getDirectors()).isEmpty()) {
            directors = List.of();
        } else {
            directors = directorService.findByIdList(dto.getDirectors().stream().mapToInt(ObjectIdDto::getId).boxed().toList());
        }

        return Film.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .genres(genres)
                .mpa(mpa)
                .directors(directors)
                .build();
    }

    public Film toEntity(FilmUpdateDto dto) {
        Film.FilmBuilder filmBuilder = Film.builder();
        filmBuilder.id(dto.getId());

        if (Optional.ofNullable(dto.getName()).isPresent()) {
            filmBuilder.name(dto.getName());
        }

        if (Optional.ofNullable(dto.getDescription()).isPresent()) {
            filmBuilder.description(dto.getDescription());
        }

        if (Optional.ofNullable(dto.getReleaseDate()).isPresent()) {
            filmBuilder.releaseDate(dto.getReleaseDate());
        }

        if (Optional.ofNullable(dto.getDuration()).isPresent()) {
            filmBuilder.duration(dto.getDuration());
        }

        if (Optional.ofNullable(dto.getGenres()).isPresent()) {
            List<Integer> genreIdList = dto.getGenres().stream().mapToInt(ObjectIdDto::getId).boxed().toList();
            filmBuilder.genres(genreService.findByIdList(genreIdList));
        }

        if (Optional.ofNullable(dto.getMpa()).isPresent()) {
            Integer mpaId = dto.getMpa().getId();
            filmBuilder.mpa(mpaService.findById(mpaId));
        }

        if (Optional.ofNullable(dto.getDirectors()).isPresent()) {
            List<Integer> directorIdList = dto.getDirectors().stream().mapToInt(ObjectIdDto::getId).boxed().toList();
            filmBuilder.directors(directorService.findByIdList(directorIdList));
        } else {
            filmBuilder.directors(List.of());
        }

        return filmBuilder.build();
    }
}
