package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.dto.ObjectIdDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.model.dto.user.UserCreateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционные тесты для класса FilmService
 * Тестирование метода findTopLiked с фильтрацией по жанру и году
 */
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmServiceTest {
    private final FilmService filmService;
    private final GenreService genreService;
    private final LikeService likeService;
    private final UserService userService;

    private Film comedyFilm2020;
    private Film comedyFilm2021;
    private Film dramaFilm2020;
    private Film dramaFilm2021;

    private List<User> testUsers;

    /**
     * Создает тестового пользователя с указанными параметрами
     */
    private User createUser(String email, String login, String name, LocalDate birthday) {
        UserCreateDto userDto = UserCreateDto.builder()
                .email(email)
                .login(login)
                .name(name)
                .birthday(birthday)
                .build();

        return userService.create(userDto);
    }

    /**
     * Подготовка тестовых данных перед каждым тестом
     * Создаем пользователей и фильмы разных жанров и годов с разным количеством лайков
     */
    @BeforeEach
    void setUp() {
        // Создаем тестовых пользователей
        testUsers = new java.util.ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            User user = createUser(
                    "user" + i + "@example.com",
                    "user" + i,
                    "User " + i,
                    LocalDate.now().minusYears(20 + i)
            );
            testUsers.add(user);
        }

        Genre comedy = genreService.findById(1); // Комедия
        Genre drama = genreService.findById(2);  // Драма

        comedyFilm2020 = createFilm("Комедия 2020", LocalDate.of(2020, 1, 1),
                List.of(comedy), 3);

        comedyFilm2021 = createFilm("Комедия 2021", LocalDate.of(2021, 1, 1),
                List.of(comedy), 1);

        dramaFilm2020 = createFilm("Драма 2020", LocalDate.of(2020, 6, 1),
                List.of(drama), 4);

        dramaFilm2021 = createFilm("Драма 2021", LocalDate.of(2021, 6, 1),
                List.of(drama), 2);
    }

    /**
     * Вспомогательный метод для создания фильма с заданными параметрами
     * и добавления указанного количества лайков
     */
    private Film createFilm(String name, LocalDate releaseDate, List<Genre> genres, int likesCount) {
        ObjectIdDto mpaDto = new ObjectIdDto();
        mpaDto.setId(1);

        List<ObjectIdDto> genreDtos = genres.stream()
                .map(genre -> {
                    ObjectIdDto dto = new ObjectIdDto();
                    dto.setId(genre.getId());
                    return dto;
                })
                .toList();

        FilmCreateDto filmDto = FilmCreateDto.builder()
                .name(name)
                .description("Описание фильма " + name)
                .releaseDate(releaseDate)
                .duration(120)
                .mpa(mpaDto) // G - General Audiences
                .genres(genreDtos)
                .build();

        Film film = filmService.create(filmDto);

        int actualLikesCount = Math.min(likesCount, testUsers.size());
        for (int i = 0; i < actualLikesCount; i++) {
            User user = testUsers.get(i);
            likeService.addLike(film.getId(), user.getId());
        }

        return film;
    }

    /**
     * Тест метода findTopLiked без фильтрации
     * Проверяем, что фильмы возвращаются в порядке убывания количества лайков
     */
    @Test
    void findTopLikedWithoutFilters_shouldReturnFilmsSortedByLikesDesc() {
        List<Film> topFilms = filmService.findTopLiked(10);


        int dramaFilm2020Index = getFilmIndexInList(topFilms, dramaFilm2020.getId());
        int comedyFilm2020Index = getFilmIndexInList(topFilms, comedyFilm2020.getId());
        int dramaFilm2021Index = getFilmIndexInList(topFilms, dramaFilm2021.getId());
        int comedyFilm2021Index = getFilmIndexInList(topFilms, comedyFilm2021.getId());

        assertThat(dramaFilm2020Index).isNotEqualTo(-1);
        assertThat(comedyFilm2020Index).isNotEqualTo(-1);
        assertThat(dramaFilm2021Index).isNotEqualTo(-1);
        assertThat(comedyFilm2021Index).isNotEqualTo(-1);

        assertThat(dramaFilm2020Index).isLessThan(comedyFilm2020Index);
        assertThat(comedyFilm2020Index).isLessThan(dramaFilm2021Index);
        assertThat(dramaFilm2021Index).isLessThan(comedyFilm2021Index);
    }

    /**
     * Вспомогательный метод для поиска индекса фильма в списке по его ID
     */
    private int getFilmIndexInList(List<Film> films, int filmId) {
        for (int i = 0; i < films.size(); i++) {
            if (films.get(i).getId() == filmId) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Тест метода findTopLiked с фильтрацией по жанру
     * Проверяем, что возвращаются только фильмы указанного жанра
     */
    @Test
    void findTopLikedWithGenreFilter_shouldReturnOnlyFilmsOfSpecifiedGenre() {
        List<Film> comedyFilms = filmService.findTopLiked(10, 1, null);

        assertThat(comedyFilms).extracting(Film::getId)
                .contains(comedyFilm2020.getId(), comedyFilm2021.getId());


        assertThat(comedyFilms).extracting(Film::getId)
                .doesNotContain(dramaFilm2020.getId(), dramaFilm2021.getId());


        int comedy2020Index = getFilmIndexInList(comedyFilms, comedyFilm2020.getId());
        int comedy2021Index = getFilmIndexInList(comedyFilms, comedyFilm2021.getId());
        assertThat(comedy2020Index).isLessThan(comedy2021Index);

        List<Film> dramaFilms = filmService.findTopLiked(10, 2, null);

        assertThat(dramaFilms).extracting(Film::getId)
                .contains(dramaFilm2020.getId(), dramaFilm2021.getId());

        assertThat(dramaFilms).extracting(Film::getId)
                .doesNotContain(comedyFilm2020.getId(), comedyFilm2021.getId());

        int drama2020Index = getFilmIndexInList(dramaFilms, dramaFilm2020.getId());
        int drama2021Index = getFilmIndexInList(dramaFilms, dramaFilm2021.getId());
        assertThat(drama2020Index).isLessThan(drama2021Index);
    }

    /**
     * Тест метода findTopLiked с фильтрацией по году
     * Проверяем, что возвращаются только фильмы указанного года
     */
    @Test
    void findTopLikedWithYearFilter_shouldReturnOnlyFilmsOfSpecifiedYear() {
        List<Film> films2020 = filmService.findTopLiked(10, null, 2020);

        assertThat(films2020).hasSize(2);
        assertThat(films2020).extracting(Film::getId)
                .containsExactly(
                        dramaFilm2020.getId(),    // 4 лайка
                        comedyFilm2020.getId()    // 3 лайка
                );

        List<Film> films2021 = filmService.findTopLiked(10, null, 2021);

        assertThat(films2021).hasSize(2);
        assertThat(films2021).extracting(Film::getId)
                .containsExactly(
                        dramaFilm2021.getId(),    // 2 лайка
                        comedyFilm2021.getId()    // 1 лайк
                );
    }

    /**
     * Тест метода findTopLiked с фильтрацией по жанру и году
     * Проверяем, что возвращаются только фильмы указанного жанра и года
     */
    @Test
    void findTopLikedWithGenreAndYearFilters_shouldReturnOnlyFilmsOfSpecifiedGenreAndYear() {
        List<Film> comedyFilms2020 = filmService.findTopLiked(10, 1, 2020);

        assertThat(comedyFilms2020).hasSize(1);
        assertThat(comedyFilms2020.getFirst().getId()).isEqualTo(comedyFilm2020.getId());

        List<Film> dramaFilms2021 = filmService.findTopLiked(10, 2, 2021);

        assertThat(dramaFilms2021).hasSize(1);
        assertThat(dramaFilms2021.getFirst().getId()).isEqualTo(dramaFilm2021.getId());
    }

    /**
     * Тест метода findTopLiked с ограничением количества результатов
     * Проверяем, что возвращается не более указанного количества фильмов
     */
    @Test
    void findTopLikedWithLimit_shouldReturnLimitedNumberOfFilms() {
        List<Film> topTwoFilms = filmService.findTopLiked(2);

        assertThat(topTwoFilms).hasSizeLessThanOrEqualTo(2);

        boolean containsDrama2020 = topTwoFilms.stream()
                .anyMatch(film -> film.getId().equals(dramaFilm2020.getId()));
        assertThat(containsDrama2020).isTrue();

        List<Film> topOneComedyFilm = filmService.findTopLiked(1, 1, null);

        assertThat(topOneComedyFilm).hasSizeLessThanOrEqualTo(1);

        if (!topOneComedyFilm.isEmpty()) {
            Film film = topOneComedyFilm.getFirst();
            boolean isComedy = film.getGenres().stream()
                    .anyMatch(genre -> genre.getId() == 1);
            assertThat(isComedy).as("Фильм должен быть комедией").isTrue();
        }
    }

    /**
     * Тест метода findTopLiked с несуществующим жанром
     * Проверяем, что метод выбрасывает исключение NotFoundException
     */
    @Test
    void findTopLikedWithInvalidGenreId_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> filmService.findTopLiked(10, 999, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Жанр id=999 не найден");
    }

    /**
     * Тест метода findTopLiked с годом, для которого нет фильмов
     * Проверяем, что возвращается пустой список
     */
    @Test
    void findTopLikedWithNonExistentYear_shouldReturnEmptyList() {
        List<Film> films2022 = filmService.findTopLiked(10, null, 2022);

        assertThat(films2022).isEmpty();
    }

    /**
     * Тест метода findCommonFilms:
     * должны возвращаться только общие для двух пользователей фильмы,
     * отсортированные по популярности (количеству лайков).
     */
    @Test
    void findCommonFilms_shouldReturnIntersectionSortedByPopularity() {
        Integer userId = testUsers.get(0).getId();   // первый пользователь
        Integer friendId = testUsers.get(1).getId(); // второй пользователь

        List<Film> commonFilms = filmService.findCommonFilms(userId, friendId);

        assertThat(commonFilms)
                .extracting(Film::getId)
                .containsExactly(
                        dramaFilm2020.getId(),
                        comedyFilm2020.getId(),
                        dramaFilm2021.getId()
                );
    }

    /**
     * Тест метода findCommonFilms с несуществующим пользователем:
     * ожидаем NotFoundException от валидатора.
     */
    @Test
    void findCommonFilms_withNonExistentUser_shouldThrowNotFound() {
        Integer existingUserId = testUsers.get(0).getId();
        Integer nonExistentUserId = 9999;

        assertThatThrownBy(() -> filmService.findCommonFilms(nonExistentUserId, existingUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь id=" + nonExistentUserId + " не найден");
    }

    /**
     * Тест метода findCommonFilms:
     * оба пользователя существуют, но у одного из них нет лайков,
     * поэтому общие фильмы отсутствуют и должен вернуться пустой список.
     * <p>
     * В setUp() у нас 5 пользователей, лайки раздаются только первым четырём,
     * пятый пользователь (testUsers.get(4)) не ставит лайков ни одному фильму.
     */
    @Test
    void findCommonFilms_whenUsersHaveNoCommonLikes_shouldReturnEmptyList() {
        Integer userIdWithLikes = testUsers.get(0).getId();   // ставил лайки
        Integer userIdWithoutLikes = testUsers.get(4).getId(); // не ставил лайков

        List<Film> commonFilms = filmService.findCommonFilms(userIdWithLikes, userIdWithoutLikes);

        assertThat(commonFilms).isEmpty();
    }

    /**
     * Тест метода findCommonFilms с несуществующим другом:
     * ожидаем NotFoundException от валидатора для friendId.
     */
    @Test
    void findCommonFilms_withNonExistentFriend_shouldThrowNotFound() {
        Integer existingUserId = testUsers.get(0).getId();
        Integer nonExistentFriendId = 9999;

        assertThatThrownBy(() -> filmService.findCommonFilms(existingUserId, nonExistentFriendId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь id=" + nonExistentFriendId + " не найден");
    }
}