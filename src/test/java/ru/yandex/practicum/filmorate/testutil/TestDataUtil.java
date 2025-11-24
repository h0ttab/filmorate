package ru.yandex.practicum.filmorate.testutil;

import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;

public final class TestDataUtil {

    private TestDataUtil() {
    }

    public static void seedAllBase(JdbcTemplate jdbcTemplate) {
        clearDomainTables(jdbcTemplate);

        seedUsers(jdbcTemplate);
        seedFriendships(jdbcTemplate);

        seedFilms(jdbcTemplate);
        seedFilmGenres(jdbcTemplate);

        seedLikes(jdbcTemplate);

        seedDirectors(jdbcTemplate);
        seedFilmDirectors(jdbcTemplate);
    }

    private static void clearDomainTables(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("DELETE FROM film_director");
        jdbcTemplate.update("DELETE FROM director");
        jdbcTemplate.update("DELETE FROM \"like\"");
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM film");
        jdbcTemplate.update("DELETE FROM friends");
        jdbcTemplate.update("DELETE FROM \"user\"");
        jdbcTemplate.update("ALTER TABLE film ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE director ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE \"user\" ALTER COLUMN id RESTART WITH 1");
    }

    public static void seedUsers(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(
                "INSERT INTO \"user\" (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "ivan.petrov@example.com", "ivan_p", "Иван Петров",
                LocalDate.of(1990, 5, 15)
        );

        jdbcTemplate.update(
                "INSERT INTO \"user\" (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "maria.sidorova@example.com", "maria_s", "Мария Сидорова",
                LocalDate.of(1995, 8, 20)
        );

        jdbcTemplate.update(
                "INSERT INTO \"user\" (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "alex.ivanov@example.com", "alex_i", "Алекс Иванов",
                LocalDate.of(1988, 3, 10)
        );
    }

    public static void seedFriendships(JdbcTemplate jdbcTemplate) {

        // user 1 -> user 2 (accepted)
        jdbcTemplate.update(
                "INSERT INTO friends (request_from_id, request_to_id, is_accepted) VALUES (?, ?, ?)",
                1, 2, true
        );

        // user 1 -> user 3 (accepted)
        jdbcTemplate.update(
                "INSERT INTO friends (request_from_id, request_to_id, is_accepted) VALUES (?, ?, ?)",
                1, 3, true
        );

        // user 3 -> user 2 — чтобы был общий друг для commonFriends(1,3) = user 2
        jdbcTemplate.update(
                "INSERT INTO friends (request_from_id, request_to_id, is_accepted) VALUES (?, ?, ?)",
                3, 2, true
        );
    }

    public static void seedFilms(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(
                "INSERT INTO film (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                "Криминальное чтиво",
                "Фильм Квентина Тарантино с переплетением историй.",
                LocalDate.of(1994, 10, 14),
                154,
                3
        );

        jdbcTemplate.update(
                "INSERT INTO film (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                "Новый фильм про друзей",
                "Драма о дружбе и выборе.",
                LocalDate.of(1999, 4, 30),
                120,
                2
        );

        jdbcTemplate.update(
                "INSERT INTO film (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                "Шрек",
                "История про зеленого огра и его приключения.",
                LocalDate.of(2001, 5, 18),
                90,
                1
        );
    }

    public static void seedFilmGenres(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", 1, 1);
        jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", 1, 2);

        jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", 2, 2);

        jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", 3, 3);
    }

    public static void seedLikes(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("INSERT INTO \"like\" (film_id, user_id) VALUES (?, ?)", 3, 1);
        jdbcTemplate.update("INSERT INTO \"like\" (film_id, user_id) VALUES (?, ?)", 3, 2);
        jdbcTemplate.update("INSERT INTO \"like\" (film_id, user_id) VALUES (?, ?)", 3, 3);

        jdbcTemplate.update("INSERT INTO \"like\" (film_id, user_id) VALUES (?, ?)", 1, 1);
        jdbcTemplate.update("INSERT INTO \"like\" (film_id, user_id) VALUES (?, ?)", 1, 2);
    }

    public static void seedDirectors(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("INSERT INTO director (name) VALUES (?)", "Роберт Земекис");
        jdbcTemplate.update("INSERT INTO director (name) VALUES (?)", "Квентин Тарантино");
        jdbcTemplate.update("INSERT INTO director (name) VALUES (?)", "Эндрю Адамсон");
        jdbcTemplate.update("INSERT INTO director (name) VALUES (?)", "Вики Дженсон");
        jdbcTemplate.update("INSERT INTO director (name) VALUES (?)", "Томми Ли Уоллес");
    }

    public static void seedFilmDirectors(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("INSERT INTO film_director (film_id, director_id) VALUES (?, ?)", 1, 1);
        jdbcTemplate.update("INSERT INTO film_director (film_id, director_id) VALUES (?, ?)", 3, 3);
        jdbcTemplate.update("INSERT INTO film_director (film_id, director_id) VALUES (?, ?)", 3, 4);
    }

    public static void seedGenreFilmRelations(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("""
                    INSERT INTO film (id, name, description, release_date, duration, mpa_id)
                    VALUES (1, 'Film for genre test', 'desc', '2000-01-01', 100, 1)
                """);

        jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (1, 2)");
        jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (1, 6)");
    }

    public static void seedGenreBase(JdbcTemplate jdbcTemplate) {
        seedGenreFilmRelations(jdbcTemplate);
    }
}
