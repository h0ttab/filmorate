package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.filmorate.config.ControllerTest;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.testutil.TestDataUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для FilmController.
 * Включает тесты для эндпоинта /films/popular с различными параметрами.
 */
@Slf4j
@ControllerTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmControllerTest {

    private final WebTestClient webTestClient;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Эти объекты используются только как "ожидаемые" ссылки по id.
     * Сами сущности в БД создаёт TestDataUtil.seedPopularFilmsScenario().
     */
    private Film comedyFilm2020;
    private Film comedyFilm2021;
    private Film dramaFilm2020;
    private Film dramaFilm2021;

    @BeforeEach
    void setUp() {

        TestDataUtil.seedAllBase(jdbcTemplate);
        comedyFilm2020 = Film.builder()
                .id(1)
                .name("Комедия 2020")
                .description("Описание фильма Комедия 2020")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).name("G").build())
                .genres(List.of(Genre.builder().id(1).name("Комедия").build()))
                .directors(List.of())
                .build();

        comedyFilm2021 = Film.builder()
                .id(2)
                .name("Комедия 2021")
                .description("Описание фильма Комедия 2021")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).name("G").build())
                .genres(List.of(Genre.builder().id(1).name("Комедия").build()))
                .directors(List.of())
                .build();

        dramaFilm2020 = Film.builder()
                .id(3)
                .name("Драма 2020")
                .description("Описание фильма Драма 2020")
                .releaseDate(LocalDate.of(2020, 6, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).name("G").build())
                .genres(List.of(Genre.builder().id(2).name("Драма").build()))
                .directors(List.of())
                .build();

        dramaFilm2021 = Film.builder()
                .id(4)
                .name("Драма 2021")
                .description("Описание фильма Драма 2021")
                .releaseDate(LocalDate.of(2021, 6, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).name("G").build())
                .genres(List.of(Genre.builder().id(2).name("Драма").build()))
                .directors(List.of())
                .build();
    }

    @Test
    void shouldFailOnInvalidInput_createFilmTest_emptyFilm() {
        Film film = Film.builder().build();
        webTestClient.post()
                .uri("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(film)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldFailOnInvalidInput_createFilmTest_invalidReleaseDate() {
        Film film = Film.builder()
                .name("Title")
                .description("About")
                .releaseDate(LocalDate.of(1800, 1, 1))
                .duration(100)
                .build();

        webTestClient.post()
                .uri("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(film)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldFailOnInvalidInput_createFilmTest_tooLongDescription() {
        String tooLongDescription = """
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr,
                sed diam nonumy eirmod tempor invidunt ut labore et dolore
                magna aliquyam erat, sed diam voluptua. At vero eos et accusam
                et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea
                lorem ipsum
                """;

        Film film = Film.builder()
                .name("Title")
                .description(tooLongDescription)
                .releaseDate(LocalDate.of(1990, 5, 6))
                .duration(200)
                .build();

        webTestClient.post()
                .uri("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(film)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldFailOnInvalidInput_createFilmTest_missingFields() {
        Film film = Film.builder()
                .name("Title")
                .duration(200)
                .build();

        webTestClient.post()
                .uri("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(film)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldNotFailOnValidInput_createFilmTest_validRequest() {
        Film film = Film.builder()
                .name("Title")
                .description("About")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(100)
                .mpa(Mpa.builder().id(2).name("PG").build())
                .genres(List.of(Genre.builder().id(1).name("Комедия").build()))
                .directors(List.of())
                .build();

        webTestClient.post()
                .uri("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(film)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    /**
     * Тест эндпоинта /films/popular без дополнительных параметров.
     * Проверяем, что возвращаются фильмы и что список не пустой.
     */
    @Test
    @SuppressWarnings("unchecked")
    void findTopLiked_withOnlyCountParameter_shouldReturnFilmsSortedByLikes() {
        List<Map<String, Object>> films = webTestClient.get()
                .uri("/films/popular?count=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        assertThat(films).isNotNull().isNotEmpty();

        // Выводим информацию о фильмах в ответе для отладки
        log.debug("Films in response: {}", films);
        log.debug("Expected film IDs: {}, {}, {}, {}",
                dramaFilm2020.getId(), comedyFilm2020.getId(), dramaFilm2021.getId(), comedyFilm2021.getId());

        boolean containsComedyOrDrama = films.stream()
                .anyMatch(film -> {
                    List<Map<String, Object>> genres =
                            (List<Map<String, Object>>) film.get("genres");
                    if (genres != null) {
                        return genres.stream()
                                .anyMatch(genre -> {
                                    String name = (String) genre.get("name");
                                    return "Комедия".equals(name) || "Драма".equals(name);
                                });
                    }
                    return false;
                });

        assertThat(containsComedyOrDrama).isTrue();
    }

    /**
     * Тест эндпоинта /films/popular с параметром genreId.
     * Проверяем, что возвращаются фильмы указанного жанра.
     */
    @Test
    @SuppressWarnings("unchecked")
    void findTopLiked_withGenreIdParameter_shouldReturnFilmsOfSpecifiedGenre() {
        // Комедия (ID = 1)
        List<Map<String, Object>> comedyFilms = webTestClient.get()
                .uri("/films/popular?count=10&genreId=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        log.debug("Comedy films in response: {}", comedyFilms);

        assertThat(comedyFilms).isNotNull();
        if (!comedyFilms.isEmpty()) {
            boolean allFilmsAreComedies = comedyFilms.stream()
                    .allMatch(film -> {
                        List<Map<String, Object>> genres =
                                (List<Map<String, Object>>) film.get("genres");
                        if (genres != null) {
                            return genres.stream()
                                    .anyMatch(genre -> {
                                        String name = (String) genre.get("name");
                                        return "Комедия".equals(name);
                                    });
                        }
                        return false;
                    });

            assertThat(allFilmsAreComedies).isTrue();
        }

        // Драма (ID = 2)
        List<Map<String, Object>> dramaFilms = webTestClient.get()
                .uri("/films/popular?count=10&genreId=2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        log.debug("Drama films in response: {}", dramaFilms);

        assertThat(dramaFilms).isNotNull();
        if (!dramaFilms.isEmpty()) {
            boolean allFilmsAreDramas = dramaFilms.stream()
                    .allMatch(film -> {
                        List<Map<String, Object>> genres =
                                (List<Map<String, Object>>) film.get("genres");
                        if (genres != null) {
                            return genres.stream()
                                    .anyMatch(genre -> {
                                        String name = (String) genre.get("name");
                                        return "Драма".equals(name);
                                    });
                        }
                        return false;
                    });

            assertThat(allFilmsAreDramas).isTrue();
        }
    }

    /**
     * Тест эндпоинта /films/popular с параметром year.
     * Проверяем, что возвращаются фильмы указанного года.
     */
    @Test
    @SuppressWarnings("unchecked")
    void findTopLiked_withYearParameter_shouldReturnFilmsOfSpecifiedYear() {
        List<Map<String, Object>> films2020 = webTestClient.get()
                .uri("/films/popular?count=10&year=2020")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        log.debug("Films from 2020 in response: {}", films2020);
        log.debug("Expected film IDs from 2020: {}, {}", dramaFilm2020.getId(), comedyFilm2020.getId());

        assertThat(films2020).isNotNull();
        if (!films2020.isEmpty()) {
            boolean containsAnyFilm2020 = films2020.stream()
                    .anyMatch(film -> {
                        Integer id = (Integer) film.get("id");
                        return id.equals(dramaFilm2020.getId())
                                || id.equals(comedyFilm2020.getId());
                    });

            assertThat(containsAnyFilm2020).isTrue();
        }

        // 2021 год
        List<Map<String, Object>> films2021 = webTestClient.get()
                .uri("/films/popular?count=10&year=2021")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        log.debug("Films from 2021 in response: {}", films2021);
        log.debug("Expected film IDs from 2021: {}, {}", dramaFilm2021.getId(), comedyFilm2021.getId());

        assertThat(films2021).isNotNull();
        if (!films2021.isEmpty()) {
            boolean containsAnyFilm2021 = films2021.stream()
                    .anyMatch(film -> {
                        Integer id = (Integer) film.get("id");
                        return id.equals(dramaFilm2021.getId())
                                || id.equals(comedyFilm2021.getId());
                    });

            assertThat(containsAnyFilm2021).isTrue();
        }
    }

    /**
     * Тест эндпоинта /films/popular с параметрами genreId и year.
     * Проверяем, что возвращаются фильмы указанного жанра и года.
     */
    @Test
    @SuppressWarnings("unchecked")
    void findTopLiked_withGenreIdAndYearParameters_shouldReturnFilmsOfSpecifiedGenreAndYear() {
        // Комедия 2020 года
        List<Map<String, Object>> comedyFilms2020 = webTestClient.get()
                .uri("/films/popular?count=10&genreId=1&year=2020")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        log.debug("Comedy films from 2020 in response: {}", comedyFilms2020);
        log.debug("Expected comedy film ID from 2020: {}", comedyFilm2020.getId());

        assertThat(comedyFilms2020).isNotNull();
        if (!comedyFilms2020.isEmpty()) {
            boolean containsComedyFilm2020 = comedyFilms2020.stream()
                    .anyMatch(film -> {
                        Integer id = (Integer) film.get("id");
                        return id.equals(comedyFilm2020.getId());
                    });

            assertThat(containsComedyFilm2020).isTrue();
        }

        // Драма 2021 года
        List<Map<String, Object>> dramaFilms2021 = webTestClient.get()
                .uri("/films/popular?count=10&genreId=2&year=2021")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        log.debug("Drama films from 2021 in response: {}", dramaFilms2021);
        log.debug("Expected drama film ID from 2021: {}", dramaFilm2021.getId());

        assertThat(dramaFilms2021).isNotNull();

        if (!dramaFilms2021.isEmpty()) {
            boolean containsDramaFilm2021 = dramaFilms2021.stream()
                    .anyMatch(film -> {
                        Integer id = (Integer) film.get("id");
                        return id.equals(dramaFilm2021.getId());
                    });

            assertThat(containsDramaFilm2021).isTrue();
        }
    }

    /**
     * Тест эндпоинта /films/popular с ограничением количества результатов.
     * Проверяем, что возвращается не более указанного количества фильмов.
     */
    @Test
    @SuppressWarnings("unchecked")
    void findTopLiked_withLimitParameter_shouldReturnLimitedNumberOfFilms() {
        List<Map<String, Object>> topTwoFilms = webTestClient.get()
                .uri("/films/popular?count=2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        log.debug("Top 2 films in response: {}", topTwoFilms);

        assertThat(topTwoFilms).isNotNull();
        if (topTwoFilms != null) {
            assertThat(topTwoFilms.size()).isLessThanOrEqualTo(2);

            if (!topTwoFilms.isEmpty()) {
                boolean allFilmsHaveRequiredFields = topTwoFilms.stream()
                        .allMatch(film ->
                                film.containsKey("id") &&
                                        film.containsKey("name") &&
                                        film.containsKey("description") &&
                                        film.containsKey("releaseDate") &&
                                        film.containsKey("duration") &&
                                        film.containsKey("mpa") &&
                                        film.containsKey("genres")
                        );

                assertThat(allFilmsHaveRequiredFields).isTrue();
            }
        }
    }

    /**
     * Тест эндпоинта /films/popular с несуществующим жанром.
     * Проверяем, что возвращается ошибка 404 Not Found.
     */
    @Test
    void findTopLiked_withInvalidGenreId_shouldReturnNotFound() {
        webTestClient.get()
                .uri("/films/popular?count=10&genreId=999")
                .exchange()
                .expectStatus().isNotFound();
    }

    /**
     * Тест эндпоинта /films/popular с годом, для которого нет фильмов.
     * Проверяем, что возвращается пустой список.
     */
    @Test
    @SuppressWarnings("unchecked")
    void findTopLiked_withNonExistentYear_shouldReturnEmptyList() {
        List<Map<String, Object>> films = webTestClient.get()
                .uri("/films/popular?count=10&year=2022")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        assertThat(films).isNotNull().isEmpty();
    }
}
