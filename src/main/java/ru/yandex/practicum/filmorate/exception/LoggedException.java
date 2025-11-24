package ru.yandex.practicum.filmorate.exception;

import java.util.List;

import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.util.Validators;

public class LoggedException {

    public static void throwNew(RuntimeException exception, Class<?> clazz) {
        Logger log = LoggerFactory.getLogger(clazz);
        log.error(exception.getMessage(), exception);
        throw exception;
    }

    public static void throwNew(ExceptionType exceptionType, Class<?> clazz, List<Integer> id) {
        Logger log = LoggerFactory.getLogger(clazz);
        RuntimeException exception;
        switch (exceptionType) {
            case INVALID_LOGIN -> {
                exception = new ValidationException("Логин не должен содержать пробелы или быть пустым");
            }
            case USER_NOT_FOUND -> {
                exception = new NotFoundException(String.format("Пользователь id=%d не найден.", id.getFirst()));
            }
            case FILM_NOT_FOUND -> {
                exception = new NotFoundException(String.format("Фильм id=%d не найден.", id.getFirst()));
            }
            case MPA_NOT_FOUND -> {
                exception = new NotFoundException(String.format("MPA id=%d не найден.", id.getFirst()));
            }
            case GENRE_NOT_FOUND -> {
                exception = new NotFoundException(String.format("Жанр id=%d не найден.", id.getFirst()));
            }
            case DIRECTOR_NOT_FOUND -> {
                exception = new NotFoundException(String.format("Режиссёр id=%d не найден", id.getFirst()));
            }
            case INVALID_FRIENDSHIP_ADD -> {
                exception = new NotFoundException(String.format("Не удалось добавить друга с id=%d пользователю id=%d."
                        + "Убедитесь, что id пользователей указаны верно.", id.get(0), id.get(1)));
            }
            case INVALID_FRIENDSHIP_REMOVE -> {
                exception = new NotFoundException(String.format("Не удалось удалить пользователя id=%d из " +
                                "друзей пользователя id=%d. Убедитесь, что id пользователей указаны верно.",
                        id.get(0), id.get(1))
                );
            }
            case USER_LIKE_ALREADY_EXISTS -> {
                exception = new ValidationException(
                        String.format("Пользователь id=%d уже поставил лайк фильму id=%d", id.get(0), id.get(1))
                );
            }
            case USER_LIKE_NOT_EXISTS -> {
                exception = new NotFoundException(
                        String.format("Ошибка при удалении лайка. Пользователь id=%d не ставил лайк фильму id=%d.",
                                id.get(0), id.get(1))
                );
            }
            case REVIEW_NOT_FOUND -> {
                exception = new NotFoundException(String.format("Отзыв id=%d не найден.", id.getFirst()));
            }
            case REVIEW_FEEDBACK_ALREADY_EXISTS -> {
                exception = new ValidationException(
                        String.format("Пользователь id=%d уже оставил реакцию на отзыв id=%d.", id.get(1), id.get(0))
                );
            }
            case REVIEW_FEEDBACK_NOT_EXISTS -> {
                exception = new NotFoundException(
                        String.format("Реакция пользователя id=%d на отзыв id=%d отсутствует.", id.get(1), id.get(0))
                );
            }
            case INVALID_FILM_RELEASE_DATE -> {
                exception = new ValidationException("Дата создания фильма не может быть ранее 28 декабря 1895 г.");
            }
            case INVALID_FILM_DESCRIPTION -> {
                exception = new ValidationException(String.format("Длина описания фильма id=%d превышает %d символов",
                        id.getFirst(), Validators.MAX_FILM_DESCRIPTION_LENGTH)
                );
            }
            case INVALID_SORT_ORDER -> {
                exception = new ValidationException("Некорректный параметр \"sortBy\"");
            }
            case UNEXPECTED_ERROR -> {
                exception = new RuntimeException("Произошла непредвиденная ошибка при обработке запроса.");
            }
            case REQUIRED_FIELD_MISSING -> {
                exception = new ValidationException("В теле запроса отсутствует одно или несколько обязательных полей.");
            }
            case INVALID_SEARCH_REQUEST -> {
                exception = new ValidationException("Некорректно составлен поисковый запрос, или"
                        + " выбраны некорректные критерии поиска.");
            }
            default -> exception = new RuntimeException("Произошла непредвиденная ошибка при обработке запроса.");
        }
        log.error(exception.getMessage(), exception);
        throw exception;
    }
}
