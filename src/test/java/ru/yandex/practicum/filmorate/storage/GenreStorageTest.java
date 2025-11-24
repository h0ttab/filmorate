package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.testutil.TestDataUtil;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(GenreDbStorage.class)
public class GenreStorageTest {

    private final GenreDbStorage storage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        TestDataUtil.seedGenreBase(jdbcTemplate);
    }

    @Test
    void testFindById() {
        Genre genre = storage.findById(1);

        assertThat(genre)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Комедия");
    }

    @Test
    void testFindAll() {
        List<Genre> genres = storage.findAll();

        assertThat(genres.size()).isEqualTo(6);
        assertThat(genres).allSatisfy(g ->
                assertThat(g)
                        .hasFieldOrProperty("id")
                        .hasFieldOrProperty("name")
                        .hasNoNullFieldsOrProperties()
        );
    }

    @Test
    void testFindByFilmId() {
        List<Integer> genreIds = storage.findByFilmId(1)
                .stream()
                .mapToInt(Genre::getId)
                .boxed()
                .toList();

        assertThat(genreIds).containsAll(List.of(2, 6));
    }

    @Test
    void linkGenresToFilm() {
        storage.linkGenresToFilm(1, Set.of(3), false);

        List<Integer> genreIds = storage.findByFilmId(1)
                .stream()
                .mapToInt(Genre::getId)
                .boxed()
                .toList();

        assertThat(genreIds).containsAll(List.of(2, 6, 3));
    }
}
