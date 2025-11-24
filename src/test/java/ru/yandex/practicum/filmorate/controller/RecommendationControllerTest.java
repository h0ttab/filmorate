package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.RecommendationService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты для API-эндпоинта GET /users/{id}/recommendations.
 * Использует Mockito для изоляции тестирования контроллера без поднятия контекста Spring.
 */
@ExtendWith(MockitoExtension.class)
public class RecommendationControllerTest {

    private final Integer userId = 1;
    @Mock
    private RecommendationService recommendationService;
    @InjectMocks
    private UserController userController;
    private MockMvc mockMvc;
    private Film film1;
    private Film film2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

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
     * Тест API-эндпоинта GET /users/{id}/recommendations.
     * Проверяет, что эндпоинт возвращает список рекомендованных фильмов
     * с правильными данными и статусом 200 OK.
     */
    @Test
    void getRecommendationsTest() throws Exception {
        List<Film> recommendations = List.of(film1, film2);

        when(recommendationService.getRecommendations(userId)).thenReturn(recommendations);

        mockMvc.perform(get("/users/{id}/recommendations", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(film1.getId()))
                .andExpect(jsonPath("$[0].name").value(film1.getName()))
                .andExpect(jsonPath("$[1].id").value(film2.getId()))
                .andExpect(jsonPath("$[1].name").value(film2.getName()));
    }

    /**
     * Тест API-эндпоинта GET /users/{id}/recommendations при пустом списке рекомендаций.
     * Проверяет, что эндпоинт возвращает пустой список и статус 200 OK,
     * когда нет рекомендаций для пользователя.
     */
    @Test
    void getRecommendationsEmptyListTest() throws Exception {
        List<Film> recommendations = List.of();

        when(recommendationService.getRecommendations(userId)).thenReturn(recommendations);

        mockMvc.perform(get("/users/{id}/recommendations", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}