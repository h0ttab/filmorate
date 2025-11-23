package ru.yandex.practicum.filmorate.storage.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorage implements UserStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<User> mapper = new UserRowMapper();

    @Override
    public List<User> findAll() {
        String query = "SELECT * FROM \"user\";";
        return namedParameterJdbcTemplate.query(query, mapper);
    }

    @Override
    public User findById(Integer userId) {
        String query = "SELECT * FROM \"user\" WHERE id = :id;";
        MapSqlParameterSource params = new MapSqlParameterSource("id", userId);
        List<User> result = namedParameterJdbcTemplate.query(query, params, mapper);
        if (result.isEmpty()) {
            LoggedException.throwNew(ExceptionType.USER_NOT_FOUND, getClass(), List.of(userId));
        }
        return result.getFirst();
    }

    @Override
    public User create(User user) {
        String query = """
                INSERT INTO "user" (EMAIL, LOGIN, NAME, BIRTHDAY)
                VALUES (:email, :login, :name, :birthday);
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday());

        namedParameterJdbcTemplate.update(query, params, keyHolder, new String[]{"id"});

        if (Optional.ofNullable(keyHolder.getKey()).isPresent()) {
            user.setId(keyHolder.getKey().intValue());
            log.info("Добавлен новый пользователь: {}", user);
            return user;
        }

        throw new RuntimeException("Непредвиденная ошибка при добавлении пользователя.");
    }

    @Override
    public User update(User user) {
        String query = """
                    UPDATE "user"
                    SET email = :email, login = :login, name = :name, birthday = :birthday
                    WHERE "user".id = :id;
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday())
                .addValue("id", user.getId());

        int updatedRows = namedParameterJdbcTemplate.update(query, params);

        if (updatedRows == 0) {
            LoggedException.throwNew(ExceptionType.USER_NOT_FOUND, getClass(), List.of(user.getId()));
        }
        log.info("Обновлён пользователь id {}. Новое значение: {}", user.getId(), user);
        return user;
    }

    @Override
    public Integer delete(Integer userId) {
        String query = "DELETE FROM \"user\" WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", userId);
        int deletedRows = namedParameterJdbcTemplate.update(query, params);
        if (deletedRows == 0) {
            LoggedException.throwNew(ExceptionType.USER_NOT_FOUND, getClass(), List.of(userId));
        }
        log.info("Удалён пользователь id {}", userId);
        return userId;
    }

    @Override
    public List<User> getFriends(Integer userId) {
        findById(userId);
        String query = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friends f
                JOIN "user" u ON f.request_to_id = u.id
                WHERE f.request_from_id = :userId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        List<User> response = namedParameterJdbcTemplate.query(query, params, mapper);
        if (response.isEmpty()) {
            response = new ArrayList<>();
        }
        return response;
    }

    @Override
    public List<User> getCommonFriends(Integer userIdA, Integer userIdB) {
        String query = """
                SELECT u.* FROM "user" u
                JOIN friends a
                  ON a.request_to_id = u.id
                JOIN friends b
                  ON a.request_to_id = b.request_to_id
                WHERE a.request_from_id = :userIdA
                  AND b.request_from_id = :userIdB;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userIdA", userIdA)
                .addValue("userIdB", userIdB);

        return namedParameterJdbcTemplate.query(query, params, mapper);
    }

    @Override
    public void addFriend(Integer userIdA, Integer userIdB) {
        String query = """
                INSERT INTO FRIENDS (REQUEST_FROM_ID, REQUEST_TO_ID)
                values(:userIdA, :userIdB);
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userIdA", userIdA)
                .addValue("userIdB", userIdB);
        namedParameterJdbcTemplate.update(query, params);
    }

    @Override
    public void removeFriend(Integer userIdA, Integer userIdB) {
        String query = """
                DELETE FROM friends
                WHERE request_from_id = :userIdA
                AND request_to_id = :userIdB;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userIdA", userIdA)
                .addValue("userIdB", userIdB);
        namedParameterJdbcTemplate.update(query, params);
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return User.builder()
                    .id(resultSet.getInt("ID"))
                    .email(resultSet.getString("EMAIL"))
                    .login(resultSet.getString("LOGIN"))
                    .name(resultSet.getString("NAME"))
                    .birthday(resultSet.getDate("BIRTHDAY").toLocalDate())
                    .build();
        }
    }
}