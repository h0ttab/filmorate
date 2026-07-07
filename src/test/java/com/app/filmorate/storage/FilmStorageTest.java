package com.app.filmorate.storage;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.app.filmorate.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import com.app.filmorate.exception.NotFoundException;
import com.app.filmorate.model.Film;
import com.app.filmorate.model.Mpa;
import com.app.filmorate.service.*;
import com.app.filmorate.storage.director.DirectorDbStorage;
import com.app.filmorate.storage.feed.FeedDbStorage;
import com.app.filmorate.storage.film.FilmDbStorage;
import com.app.filmorate.storage.film.FilmDbStorage.FilmRowMapper;
import com.app.filmorate.storage.genre.GenreDbStorage;
import com.app.filmorate.storage.like.LikeDbStorage;
import com.app.filmorate.storage.mpa.MpaDbStorage;
import com.app.filmorate.testutil.TestDataUtil;
import com.app.filmorate.util.Validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmDbStorage.class,
        MpaService.class,
        MpaDbStorage.class,
        Validators.class,
        GenreService.class,
        GenreDbStorage.class,
        FilmRowMapper.class,
        FeedDbStorage.class,
        FeedService.class,
        LikeDbStorage.class,
        LikeService.class,
        DirectorService.class,
        DirectorDbStorage.class
})
public class FilmStorageTest {

    private final FilmDbStorage storage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        TestDataUtil.seedAllBase(jdbcTemplate);
    }

    private void assertFilm(Film film, Integer id, String name, String description,
                            LocalDate releaseDate, Integer duration) {
        assertThat(film)
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("name", name)
                .hasFieldOrPropertyWithValue("description", description)
                .hasFieldOrPropertyWithValue("releaseDate", releaseDate)
                .hasFieldOrPropertyWithValue("duration", duration);
    }

    @Test
    void testFindById() {
        Film film = storage.findById(3);

        assertFilm(
                film,
                3,
                "Шрек",
                "История про зеленого огра и его приключения.",
                LocalDate.of(2001, 5, 18),
                90
        );
    }

    @Test
    void testFindAll() {
        List<Film> films = storage.findAll();

        assertThat(films)
                .hasSize(3)
                .extracting(Film::getId)
                .containsExactlyInAnyOrder(1, 2, 3);

        films.stream()
                .filter(f -> f.getId() == 3)
                .findFirst()
                .ifPresent(f -> assertFilm(
                        f,
                        3,
                        "Шрек",
                        "История про зеленого огра и его приключения.",
                        LocalDate.of(2001, 5, 18),
                        90
                ));
    }

    @Test
    void testFilmCreate() {
        Film film = Film.builder()
                .name("Оно")
                .description("Леденящее воплощение ужаса притаилось в тени и повсюду")
                .releaseDate(LocalDate.of(1990, 11, 18))
                .duration(192)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film createdFilm = storage.create(film);
        Film createdFilmFromDb = storage.findById(createdFilm.getId());

        assertFilm(
                createdFilmFromDb,
                createdFilm.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration()
        );
    }

    @Test
    void testFilmUpdate() {
        Film originalFilm = storage.findById(1);

        assertThat(originalFilm.getName()).isEqualTo("Криминальное чтиво");

        Film updated = originalFilm.toBuilder()
                .name("UPDATED NAME")
                .build();

        storage.update(updated);

        Film updatedFilm = storage.findById(1);

        assertThat(updatedFilm.getName()).isEqualTo("UPDATED NAME");
    }

    @Test
    void testFilmDelete() {
        assertDoesNotThrow(() -> storage.findById(1));

        storage.delete(1);

        assertThrows(NotFoundException.class, () -> storage.findById(1));
    }

    @Test
    void testFindTopLiked() {
        List<Film> films = storage.findTopLiked(3);

        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(3, 1, 2);
    }

    @Test
    void testFindCommonFilms_shouldReturnIntersectionSortedByPopularity() {
        List<Film> films = storage.findCommonFilms(1, 2);

        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(3, 1);
    }

    @Test
    void testFindCommonFilms_whenNoCommonLikes_shouldReturnEmptyList() {
        List<Film> films = storage.findCommonFilms(1, 999);

        assertThat(films).isEmpty();
    }
}
