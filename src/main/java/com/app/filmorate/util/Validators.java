package com.app.filmorate.util;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.app.filmorate.exception.ExceptionType;
import com.app.filmorate.exception.LoggedException;
import com.app.filmorate.model.dto.film.FilmCreateDto;
import com.app.filmorate.model.dto.film.FilmUpdateDto;

@Component
@RequiredArgsConstructor
public class Validators {
    public static final int MAX_FILM_DESCRIPTION_LENGTH = 200;
    private final JdbcTemplate jdbcTemplate;

    public boolean isValidString(String str) {
        return str != null && !str.isBlank();
    }

    private boolean isValidLogin(String login) {
        return isValidString(login) && !login.contains(" ");
    }

    public void validateLogin(String login, Class<?> clazz) {
        if (!isValidLogin(login)) {
            LoggedException.throwNew(ExceptionType.INVALID_LOGIN, clazz, List.of());
        }
    }

    private boolean isValidFilmReleaseDate(LocalDate releaseDate) {
        if (releaseDate == null) {
            return true;
        }
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        return releaseDate.isAfter(minDate);
    }

    public void validateFilmReleaseDate(LocalDate releaseDate, Class<?> clazz) {
        if (!isValidFilmReleaseDate(releaseDate)) {
            LoggedException.throwNew(ExceptionType.INVALID_FILM_RELEASE_DATE, clazz, List.of());
        }
    }

    private boolean isValidFilmDescription(String description) {
        Optional<String> desc = Optional.ofNullable(description);
        return desc.isPresent() && desc.get().length() < Validators.MAX_FILM_DESCRIPTION_LENGTH;
    }

    public void validateFilmDescription(String description, Integer filmId, Class<?> clazz) {
        if (!isValidFilmDescription(description)) {
            LoggedException.throwNew(ExceptionType.INVALID_FILM_DESCRIPTION, clazz, List.of(filmId));
        }
    }

    private boolean isExistingLike(Integer filmId, Integer userId) {
        String query = """
                    SELECT
                    CASE
                        WHEN (SELECT count(*) FROM "like" WHERE user_id = ? AND film_id = ?) > 0 THEN TRUE
                        ELSE FALSE
                    END;
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, Boolean.class, userId, filmId));
    }

    public void validateLikeExists(Integer filmId, Integer userId, Class<?> clazz) {
        if (!isExistingLike(filmId, userId)) {
            LoggedException.throwNew(ExceptionType.USER_LIKE_NOT_EXISTS, clazz, List.of(userId, filmId));
        }
    }

    public void validateLikeNotExists(Integer filmId, Integer userId, Class<?> clazz) {
        if (isExistingLike(filmId, userId)) {
            LoggedException.throwNew(ExceptionType.USER_LIKE_ALREADY_EXISTS, clazz, List.of(userId, filmId));
        }
    }

    private boolean isValidMpa(Integer mpaId) {
        String query = """
                    SELECT
                    CASE
                    	WHEN ? IN (SELECT id FROM mpa) THEN TRUE
                    	ELSE FALSE
                    END;
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, Boolean.class, mpaId));
    }

    private boolean isValidFilm(Integer filmId) {
        String query = """
                    SELECT
                    CASE
                    	WHEN ? IN (SELECT id FROM film) THEN TRUE
                    	ELSE FALSE
                    END;
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, Boolean.class, filmId));
    }

    public void validateCreateFilmDto(FilmCreateDto dto) {
        isValidString(dto.getName());
        isValidString(dto.getDescription());
        validateFilmReleaseDate(dto.getReleaseDate(), getClass());
        validateMpaExists(dto.getMpa().getId(), getClass());
    }

    public void validateUpdateFilmDto(FilmUpdateDto dto) {
        validateFilmExists(dto.getId(), getClass());

        if (Optional.ofNullable(dto.getName()).isPresent()) {
            isValidString(dto.getName());
        }

        if (Optional.ofNullable(dto.getDescription()).isPresent()) {
            isValidString(dto.getDescription());
        }

        if (Optional.ofNullable(dto.getReleaseDate()).isPresent()) {
            validateFilmReleaseDate(dto.getReleaseDate(), getClass());
        }

        if (Optional.ofNullable(dto.getGenres()).isPresent()) {
            dto.getGenres().forEach(
                    genreIdDto -> validateGenreExists(genreIdDto.getId(), getClass())
            );
        }

        if (Optional.ofNullable(dto.getMpa()).isPresent()) {
            validateMpaExists(dto.getMpa().getId(), getClass());
        }

        if (Optional.ofNullable(dto.getDirectors()).isPresent()) {
            dto.getDirectors().forEach(
                    directorIdDto -> validateDirectorExists(directorIdDto.getId(), getClass())
            );
        }
    }

    public void validateFilmExists(Integer filmId, Class<?> clazz) {
        if (!isValidFilm(filmId)) {
            LoggedException.throwNew(ExceptionType.FILM_NOT_FOUND, clazz, List.of(filmId));
        }
    }

    public void validateMpaExists(Integer mpaId, Class<?> clazz) {
        if (!isValidMpa(mpaId)) {
            LoggedException.throwNew(ExceptionType.MPA_NOT_FOUND, clazz, List.of(mpaId));
        }
    }

    private boolean isValidGenre(Integer genreId) {
        String query = """
                    SELECT
                    CASE
                    	WHEN ? IN (SELECT id FROM genre) THEN TRUE
                    	ELSE FALSE
                    END;
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, Boolean.class, genreId));
    }

    public void validateGenreExists(Integer genreId, Class<?> clazz) {
        if (!isValidGenre(genreId)) {
            LoggedException.throwNew(ExceptionType.GENRE_NOT_FOUND, clazz, List.of(genreId));
        }
    }

    private boolean isValidDirector(Integer directorId) {
        String query = """
                    SELECT
                    CASE
                    	WHEN ? IN (SELECT id FROM director) THEN TRUE
                    	ELSE FALSE
                    END;
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, Boolean.class, directorId));
    }

    public void validateDirectorExists(Integer directorId, Class<?> clazz) {
        if (!isValidDirector(directorId)) {
            LoggedException.throwNew(ExceptionType.DIRECTOR_NOT_FOUND, clazz, List.of(directorId));
        }
    }

    private boolean isValidUser(Integer userId) {
        String query = """
                    SELECT
                    CASE
                    	WHEN ? IN (SELECT id FROM "user") THEN TRUE
                    	ELSE FALSE
                    END;
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, Boolean.class, userId));
    }

    public void validateUserExists(Integer userId, Class<?> clazz) {
        if (!isValidUser(userId)) {
            LoggedException.throwNew(ExceptionType.USER_NOT_FOUND, clazz, List.of(userId));
        }
    }

    private boolean isValidFriend(Integer userIdA, Integer userIdB) {
        String query = """
                SELECT CASE
                	WHEN (SELECT COUNT(*) FROM friends WHERE request_from_id = ? AND request_to_id = ?) > 0 THEN TRUE
                	ELSE FALSE
                END;
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, Boolean.class, userIdA, userIdB));
    }

    public void validateFriendshipExists(Integer userIdA, Integer userIdB, Class<?> clazz) {
        if (!isValidFriend(userIdA, userIdB)) {
            LoggedException.throwNew(ExceptionType.INVALID_FRIENDSHIP_REMOVE, clazz, List.of(userIdA, userIdB));
        }
    }

    public void validateFriendshipNotExists(Integer userIdA, Integer userIdB, Class<?> clazz) {
        if (isValidFriend(userIdA, userIdB)) {
            LoggedException.throwNew(ExceptionType.INVALID_FRIENDSHIP_ADD, clazz, List.of(userIdA, userIdB));
        }
    }

    private boolean isValidReview(Integer reviewId) {
        String query = """
                    SELECT
                    CASE
                        WHEN EXISTS(SELECT 1 FROM review WHERE id = ?) THEN TRUE
                        ELSE FALSE
                    END;
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, Boolean.class, reviewId));
    }

    public void validateReviewExists(Integer reviewId, Class<?> clazz) {
        if (!isValidReview(reviewId)) {
            LoggedException.throwNew(ExceptionType.REVIEW_NOT_FOUND, clazz, List.of(reviewId));
        }
    }

    private boolean isValidReviewFeedback(Integer reviewId, Integer userId) {
        String query = """
                    SELECT
                    CASE
                        WHEN EXISTS(SELECT 1 FROM review_feedback WHERE review_id = ? AND user_id = ?) THEN TRUE
                        ELSE FALSE
                    END;
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, Boolean.class, reviewId, userId));
    }

    public void validateReviewFeedbackExists(Integer reviewId, Integer userId, Class<?> clazz) {
        if (!isValidReviewFeedback(reviewId, userId)) {
            LoggedException.throwNew(ExceptionType.REVIEW_FEEDBACK_NOT_EXISTS, clazz, List.of(reviewId, userId));
        }
    }

    public void validateReviewFeedbackNotExists(Integer reviewId, Integer userId, Class<?> clazz) {
        if (isValidReviewFeedback(reviewId, userId)) {
            LoggedException.throwNew(ExceptionType.REVIEW_FEEDBACK_ALREADY_EXISTS, clazz, List.of(reviewId, userId));
        }
    }
}
