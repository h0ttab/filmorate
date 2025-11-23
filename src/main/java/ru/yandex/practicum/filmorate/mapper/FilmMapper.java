package ru.yandex.practicum.filmorate.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.dto.ObjectIdDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.util.Validators;

@Mapper(componentModel = "spring")
public abstract class FilmMapper {

    protected Validators validators;
    protected MpaService mpaService;
    protected GenreService genreService;
    protected DirectorService directorService;

    @Autowired
    public void setValidators(Validators validators) {
        this.validators = validators;
    }

    @Autowired
    public void setMpaService(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @Autowired
    public void setGenreService(GenreService genreService) {
        this.genreService = genreService;
    }

    @Autowired
    public void setDirectorService(DirectorService directorService) {
        this.directorService = directorService;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mpa", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "directors", ignore = true)
    protected abstract Film mapCreate(FilmCreateDto dto);

    @Mapping(target = "mpa", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "directors", ignore = true)
    protected abstract Film mapUpdate(FilmUpdateDto dto);

    public Film toEntity(FilmCreateDto dto) {
        validateCreateFilmDto(dto);

        Film film = mapCreate(dto);

        List<Genre> genres = List.of();
        if (Optional.ofNullable(dto.getGenres()).isPresent()) {
            List<Integer> genreIdList = dto.getGenres().stream()
                    .map(ObjectIdDto::getId)
                    .collect(Collectors.toList());
            genres = genreService.findByIdList(genreIdList);
        }

        if (Optional.ofNullable(dto.getDirectors()).isPresent()) {
            dto.getDirectors().stream()
                    .map(ObjectIdDto::getId)
                    .forEach(id -> validators.validateDirectorExists(id, getClass()));
        }

        var mpa = mpaService.findById(dto.getMpa().getId());

        List<Director> directors;
        if (Optional.ofNullable(dto.getDirectors()).isEmpty()) {
            directors = List.of();
        } else {
            List<Integer> directorIdList = dto.getDirectors().stream()
                    .map(ObjectIdDto::getId)
                    .collect(Collectors.toList());
            directors = directorService.findByIdList(directorIdList);
        }

        film.setGenres(genres);
        film.setMpa(mpa);
        film.setDirectors(directors);

        return film;
    }

    public Film toEntity(FilmUpdateDto dto) {
        validateUpdateFilmDto(dto);

        Film film = mapUpdate(dto);

        if (Optional.ofNullable(dto.getGenres()).isPresent()) {
            List<Integer> genreIdList = dto.getGenres().stream()
                    .map(ObjectIdDto::getId)
                    .collect(Collectors.toList());
            film.setGenres(genreService.findByIdList(genreIdList));
        }

        if (Optional.ofNullable(dto.getMpa()).isPresent()) {
            Integer mpaId = dto.getMpa().getId();
            validators.validateMpaExists(mpaId, getClass());
            film.setMpa(mpaService.findById(mpaId));
        }

        if (Optional.ofNullable(dto.getDirectors()).isPresent()) {
            List<Integer> directorIdList = dto.getDirectors().stream()
                    .map(ObjectIdDto::getId)
                    .collect(Collectors.toList());
            film.setDirectors(directorService.findByIdList(directorIdList));
        } else {
            film.setDirectors(List.of());
        }

        return film;
    }

    private void validateCreateFilmDto(FilmCreateDto dto) {
        validators.isValidString(dto.getName());
        validators.isValidString(dto.getDescription());
        validators.validateFilmReleaseDate(dto.getReleaseDate(), getClass());
        validators.validateMpaExists(dto.getMpa().getId(), getClass());
    }

    private void validateUpdateFilmDto(FilmUpdateDto dto) {
        validators.validateFilmExists(dto.getId(), getClass());

        if (Optional.ofNullable(dto.getName()).isPresent()) {
            validators.isValidString(dto.getName());
        }

        if (Optional.ofNullable(dto.getDescription()).isPresent()) {
            validators.isValidString(dto.getDescription());
        }

        if (Optional.ofNullable(dto.getReleaseDate()).isPresent()) {
            validators.validateFilmReleaseDate(dto.getReleaseDate(), getClass());
        }

        if (Optional.ofNullable(dto.getGenres()).isPresent()) {
            dto.getGenres().forEach(
                    genreIdDto -> validators.validateGenreExists(genreIdDto.getId(), getClass())
            );
        }

        if (Optional.ofNullable(dto.getMpa()).isPresent()) {
            validators.validateMpaExists(dto.getMpa().getId(), getClass());
        }

        if (Optional.ofNullable(dto.getDirectors()).isPresent()) {
            dto.getDirectors().forEach(
                    directorIdDto -> validators.validateDirectorExists(directorIdDto.getId(), getClass())
            );
        }
    }
}
