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
        return namedParameterJdbcTemplate.query(UserSqlQueries.FIND_ALL.getQuery(), mapper);
    }

    @Override
    public User findById(Integer userId) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", userId);
        List<User> result = namedParameterJdbcTemplate.query(UserSqlQueries.FIND_BY_ID.getQuery(), params, mapper);
        if (result.isEmpty()) {
            LoggedException.throwNew(ExceptionType.USER_NOT_FOUND, getClass(), List.of(userId));
        }
        return result.getFirst();
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday());

        namedParameterJdbcTemplate.update(UserSqlQueries.CREATE.getQuery(), params, keyHolder, new String[]{"id"});

        if (Optional.ofNullable(keyHolder.getKey()).isPresent()) {
            user.setId(keyHolder.getKey().intValue());
            log.info("Добавлен новый пользователь: {}", user);
            return user;
        }

        throw new RuntimeException("Непредвиденная ошибка при добавлении пользователя.");
    }

    @Override
    public User update(User user) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", user.getEmail())
                .addValue("login", user.getLogin())
                .addValue("name", user.getName())
                .addValue("birthday", user.getBirthday())
                .addValue("id", user.getId());

        int updatedRows = namedParameterJdbcTemplate.update(UserSqlQueries.UPDATE.getQuery(), params);

        if (updatedRows == 0) {
            LoggedException.throwNew(ExceptionType.USER_NOT_FOUND, getClass(), List.of(user.getId()));
        }
        log.info("Обновлён пользователь id {}. Новое значение: {}", user.getId(), user);
        return user;
    }

    @Override
    public Integer delete(Integer userId) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", userId);
        int deletedRows = namedParameterJdbcTemplate.update(UserSqlQueries.DELETE.getQuery(), params);
        if (deletedRows == 0) {
            LoggedException.throwNew(ExceptionType.USER_NOT_FOUND, getClass(), List.of(userId));
        }
        log.info("Удалён пользователь id {}", userId);
        return userId;
    }

    @Override
    public List<User> getFriends(Integer userId) {
        findById(userId);
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        List<User> response = namedParameterJdbcTemplate.query(UserSqlQueries.GET_FRIENDS.getQuery(), params, mapper);
        if (response.isEmpty()) {
            response = new ArrayList<>();
        }
        return response;
    }

    @Override
    public List<User> getCommonFriends(Integer userIdA, Integer userIdB) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userIdA", userIdA)
                .addValue("userIdB", userIdB);

        return namedParameterJdbcTemplate.query(UserSqlQueries.GET_COMMON_FRIENDS.getQuery(), params, mapper);
    }

    @Override
    public void addFriend(Integer userIdA, Integer userIdB) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userIdA", userIdA)
                .addValue("userIdB", userIdB);
        namedParameterJdbcTemplate.update(UserSqlQueries.ADD_FRIEND.getQuery(), params);
    }

    @Override
    public void removeFriend(Integer userIdA, Integer userIdB) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userIdA", userIdA)
                .addValue("userIdB", userIdB);
        namedParameterJdbcTemplate.update(UserSqlQueries.REMOVE_FRIEND.getQuery(), params);
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