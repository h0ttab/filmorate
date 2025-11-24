package ru.yandex.practicum.filmorate.storage;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        DirectorDbStorage.class,
        FilmDbStorage.class,
        MpaDbStorage.class
})
public class DirectorStorageTest {

    private final DirectorDbStorage directorStorage;
    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbc;

    @BeforeEach
    void beforeEach() {
        jdbc.update("DELETE FROM film_director");
        jdbc.update("DELETE FROM director");
    }

    private Director newDirector(String name) {
        return Director.builder().name(name).build();
    }

    private Film newFilm(String name) {
        return filmStorage.create(
                Film.builder()
                        .name(name)
                        .description("test")
                        .releaseDate(LocalDate.of(2000, 1, 1))
                        .duration(100)
                        .mpa(Mpa.builder().id(1).build())
                        .build()
        );
    }

    @Test
    void testCreateDirector() {
        Director input = newDirector("Имя Режиссёра");

        Director created = directorStorage.create(input);
        input.setId(created.getId());

        Director fromDb = directorStorage.findById(created.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created).isEqualTo(input);
        assertThat(fromDb).isEqualTo(created);
    }

    @Test
    void testFindAll() {
        Director d1 = directorStorage.create(newDirector("Режиссёр 1"));
        Director d2 = directorStorage.create(newDirector("Режиссёр 2"));

        List<Director> all = directorStorage.findAll();

        assertThat(all)
                .hasSize(2)
                .extracting(Director::getId)
                .containsExactlyInAnyOrder(d1.getId(), d2.getId());

        assertThat(all)
                .extracting(Director::getName)
                .containsExactlyInAnyOrder("Режиссёр 1", "Режиссёр 2");
    }

    @Test
    void testFindByFilm() {
        Director d1 = directorStorage.create(newDirector("Эндрю Адамсон"));
        Director d2 = directorStorage.create(newDirector("Вики Дженсон"));

        Film film = newFilm("Фильм с двумя режиссёрами");
        int filmId = film.getId();

        directorStorage.linkDirectorsToFilm(filmId, List.of(d1.getId(), d2.getId()), false);

        List<Director> directors = directorStorage.findByFilm(filmId);

        assertThat(directors)
                .hasSize(2)
                .containsExactlyInAnyOrder(d1, d2);
    }

    @Test
    void testFindById() {
        Director created = directorStorage.create(newDirector("Томми Ли Уоллес"));

        Director fromDb = directorStorage.findById(created.getId());

        assertThat(fromDb)
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("name", "Томми Ли Уоллес");
    }

    @Test
    void testUpdate() {
        Director created = directorStorage.create(newDirector("Квентин Тарантино"));

        Director updated = created.toBuilder()
                .name("Твентин Карантино")
                .build();

        directorStorage.update(updated);

        Director fromDb = directorStorage.findById(created.getId());

        assertThat(fromDb)
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("name", "Твентин Карантино");
    }

    @Test
    void testLinkDirectorsToFilm() {
        Director d1 = directorStorage.create(newDirector("Квентин Тарантино"));
        Director d2 = directorStorage.create(newDirector("Томми Ли Уоллес"));

        Film film = newFilm("Фильм для привязки");
        int filmId = film.getId();

        directorStorage.linkDirectorsToFilm(filmId, List.of(d1.getId()), false);

        assertThat(directorStorage.findByFilm(filmId))
                .containsExactly(d1);

        directorStorage.linkDirectorsToFilm(filmId, List.of(d2.getId()), false);

        assertThat(directorStorage.findByFilm(filmId))
                .containsExactlyInAnyOrder(d1, d2);
    }

    @Test
    void testDelete() {
        Director created = directorStorage.create(newDirector("Для удаления"));

        assertDoesNotThrow(() -> directorStorage.findById(created.getId()));

        directorStorage.delete(created.getId());

        assertThrows(
                NotFoundException.class,
                () -> directorStorage.findById(created.getId())
        );
    }
}
