package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.dto.ObjectIdDto;
import ru.yandex.practicum.filmorate.model.dto.film.DirectorDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.model.dto.user.UserCreateDto;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SearchServiceTest {

    private final SearchService searchService;
    private final FilmService filmService;
    private final DirectorService directorService;
    private final LikeService likeService;
    private final UserService userService;

    private List<Integer> userIds;

    private Film firstFilm;
    private Film secondFilm;
    private Film thirdFilm;

    @BeforeEach
    void setUp() {
        // создаём пользователей, которые будут ставить лайки
        userIds = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            UserCreateDto userDto = UserCreateDto.builder()
                    .email("user" + i + "@mail.com")
                    .login("user" + i)
                    .name("User " + i)
                    .birthday(LocalDate.of(1990 + i, 1, 1))
                    .build();
            userIds.add(userService.create(userDto).getId());
        }

        // режиссёр для первого фильма
        DirectorDto directorDto1 = new DirectorDto();
        directorDto1.setName("Питер Доктор");
        Director directorForFirstFilm = directorService.create(directorDto1);

        // режиссёр для второго и третьего фильмов
        DirectorDto directorDto2 = new DirectorDto();
        directorDto2.setName("Роберт Земекис");
        Director directorForSecondAndThirdFilms = directorService.create(directorDto2);

        firstFilm = createFilmWithLikes(
                "Шрек",
                "Мультфильм про огра",
                directorForFirstFilm.getId(),
                LocalDate.of(2001, 5, 18),
                3
        );

        secondFilm = createFilmWithLikes(
                "Криминальное чтиво",
                "Культовый фильм Тарантино",
                directorForSecondAndThirdFilms.getId(),
                LocalDate.of(1994, 10, 14),
                2
        );

        thirdFilm = createFilmWithLikes(
                "Форрест Гамп",
                "Фильм о жизни Форреста",
                directorForSecondAndThirdFilms.getId(),
                LocalDate.of(1994, 7, 6),
                1
        );
    }

    private Film createFilmWithLikes(String name,
                                     String description,
                                     Integer directorId,
                                     LocalDate releaseDate,
                                     int likesCount) {
        ObjectIdDto mpaDto = new ObjectIdDto();
        mpaDto.setId(1); // MPA с id=1 уже есть в data.sql

        ObjectIdDto directorIdDto = new ObjectIdDto();
        directorIdDto.setId(directorId);

        FilmCreateDto filmDto = FilmCreateDto.builder()
                .name(name)
                .description(description)
                .releaseDate(releaseDate)
                .duration(120)
                .mpa(mpaDto)
                .genres(List.of())                  // жанры не важны для этого набора тестов
                .directors(List.of(directorIdDto))  // привязка к режиссёру
                .build();

        Film film = filmService.create(filmDto);

        int realLikes = Math.min(likesCount, userIds.size());
        for (int i = 0; i < realLikes; i++) {
            likeService.addLike(film.getId(), userIds.get(i));
        }

        return filmService.findById(film.getId());
    }

    @Test
    public void searchByTitleTest() {
        // подстрока "рек" должна найти первый фильм (название содержит "Шрек")
        List<Film> result = searchService.searchFilms("рек", Set.of("title"));

        assertThat(result)
                .extracting(Film::getId)
                .contains(firstFilm.getId());
    }

    @Test
    public void searchByDirectorTest() {
        // подстрока "берт" – по режиссёру "Роберт Земекис"
        // ожидаются фильмы второго и третьего режиссёра, отсортированные по лайкам
        List<Film> result = searchService.searchFilms("берт", Set.of("director"));

        assertThat(result)
                .extracting(Film::getId)
                .containsExactly(
                        secondFilm.getId(), // 2 лайка
                        thirdFilm.getId()   // 1 лайк
                );
    }

    @Test
    public void searchByTitleAndDirectorTest() {
        // подстрока "ек":
        //  - первый фильм будет найден по title ("Шрек")
        //  - второй и третий – по режиссёру ("ЗемЕкис")
        // сортировка по популярности (кол-ву лайков): first(3), second(2), third(1)
        List<Film> result = searchService.searchFilms("ек", Set.of("director", "title"));

        assertThat(result)
                .extracting(Film::getId)
                .containsExactly(
                        firstFilm.getId(),
                        secondFilm.getId(),
                        thirdFilm.getId()
                );
    }

    @Test
    public void multipleResultTest() {
        // подстрока "м" в названии:
        //  - "КриМинальное чтиво"
        //  - "Форрест ГаМп"
        // ожидается: второй и третий фильмы по убыванию лайков
        List<Film> result = searchService.searchFilms("м", Set.of("title"));

        assertThat(result)
                .extracting(Film::getId)
                .containsExactly(
                        secondFilm.getId(),  // 2 лайка
                        thirdFilm.getId()    // 1 лайк
                );
    }

    @Test
    public void resultOrderTest() {
        // топ-3 по лайкам
        List<Film> topFilms = filmService.findTopLiked(3);
        assertThat(topFilms)
                .extracting(Film::getId)
                .containsExactly(
                        firstFilm.getId(),
                        secondFilm.getId(),
                        thirdFilm.getId()
                );

        // поиск по "е" в title должен вернуть те же фильмы в том же порядке
        List<Film> searchResult = searchService.searchFilms("е", Set.of("title"));
        assertThat(searchResult)
                .extracting(Film::getId)
                .containsExactly(
                        firstFilm.getId(),
                        secondFilm.getId(),
                        thirdFilm.getId()
                );
    }
}
