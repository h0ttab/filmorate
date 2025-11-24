package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.recommendation.RecommendationStorage;
import ru.yandex.practicum.filmorate.util.Validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Тесты для сервиса рекомендаций фильмов.
 */
@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    private final Integer userId = 1;
    @Mock
    private RecommendationStorage recommendationStorage;
    @Mock
    private Validators validators;

    @Mock
    private FilmService filmService;   // добавили мок FilmService

    @InjectMocks
    private RecommendationService recommendationService;
    private Film film1;
    private Film film2;

    @BeforeEach
    void setUp() {
        film1 = Film.builder()
                .id(1)
                .name("Фильм 1")
                .description("Описание фильма 1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).name("G").build())
                .build();

        film2 = Film.builder()
                .id(2)
                .name("Фильм 2")
                .description("Описание фильма 2")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(130)
                .mpa(Mpa.builder().id(2).name("PG").build())
                .build();
    }

    /**
     * Тест метода getRecommendations.
     * Проверяет, что метод возвращает список рекомендованных фильмов,
     * полученных из хранилища рекомендаций.
     */
    @Test
    void getRecommendationsTest() {
        // Подготавливаем данные
        List<Film> storageRecommendations = List.of(film1, film2);

        doNothing().when(validators).validateUserExists(userId, RecommendationService.class);
        when(recommendationStorage.getRecommendations(userId)).thenReturn(storageRecommendations);
        // RecommendationService делегирует обогащение фильмов в filmService.addAttributes(...)
        when(filmService.addAttributes(storageRecommendations)).thenReturn(storageRecommendations);

        List<Film> actualRecommendations = recommendationService.getRecommendations(userId);

        assertEquals(storageRecommendations.size(), actualRecommendations.size(), "Размер списка рекомендаций должен совпадать");
        assertEquals(storageRecommendations, actualRecommendations, "Списки рекомендаций должны совпадать");

        verify(validators).validateUserExists(userId, RecommendationService.class);
        verify(recommendationStorage).getRecommendations(userId);
        verify(filmService).addAttributes(storageRecommendations);
    }

    /**
     * Тест метода getRecommendations при пустом списке рекомендаций.
     * Проверяет, что метод корректно обрабатывает случай, когда нет рекомендаций для пользователя.
     */
    @Test
    void getRecommendationsEmptyListTest() {
        List<Film> storageRecommendations = List.of();

        doNothing().when(validators).validateUserExists(userId, RecommendationService.class);
        when(recommendationStorage.getRecommendations(userId)).thenReturn(storageRecommendations);
        when(filmService.addAttributes(storageRecommendations)).thenReturn(storageRecommendations);

        List<Film> actualRecommendations = recommendationService.getRecommendations(userId);

        assertEquals(0, actualRecommendations.size(), "Размер списка рекомендаций должен быть 0");

        verify(validators).validateUserExists(userId, RecommendationService.class);
        verify(recommendationStorage).getRecommendations(userId);
        verify(filmService).addAttributes(storageRecommendations);
    }
}
