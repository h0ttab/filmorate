package ru.yandex.practicum.filmorate.storage.user;

import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.AbstractInMemoryStorage;
import ru.yandex.practicum.filmorate.util.Validators;

@Component
@RequiredArgsConstructor
public class InMemoryUserStorage extends AbstractInMemoryStorage<User> implements UserStorage {
    private final Validators validators;

    public Map<Integer, User> getStorage() {
        return Map.copyOf(mapEntityStorage);
    }

    @Override
    public List<User> findAll() {
        return mapEntityStorage.values().stream().toList();
    }

    @Override
    public User findById(Integer userId) {
        return mapEntityStorage.get(userId);
    }

    @Override
    public User create(User user) {
        validators.validateLogin(user.getLogin(), getClass());
        user.setId(getNextId());
        mapEntityStorage.put(user.getId(), user);
        log.info("Добавлен новый пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        validators.validateLogin(user.getLogin(), getClass());
        log.info("Обновлён пользователь id {}. Новое значение: {}", user.getId(), user);
        return user;
    }

    @Override
    public Integer delete(Integer userId) {
        mapEntityStorage.remove(userId);
        log.info("Удалён пользователь id {}", userId);
        return userId;
    }

    @Override
    public List<User> getFriends(Integer userId) {
        validators.validateUserExists(userId, getClass());
        return mapEntityStorage.get(userId).getFriends().keySet().stream()
                .map(mapEntityStorage::get).toList();
    }

    @Override
    public List<User> getCommonFriends(Integer userIdA, Integer userIdB) {
        Set<Integer> userAFriends = findById(userIdA).getFriends().keySet();
        Set<Integer> userBFriends = findById(userIdB).getFriends().keySet();

        List<User> result = new ArrayList<>();

        for (Integer id : userAFriends) {
            if (userBFriends.contains(id)) {
                result.add(findById(id));
            }
        }

        return result;
    }

    @Override
    public void addFriend(Integer userIdA, Integer userIdB) {
        Optional<User> userA = Optional.ofNullable(findById(userIdA));
        Optional<User> userB = Optional.ofNullable(findById(userIdB));

        if (userA.isPresent() && userB.isPresent()) {
            userA.get().getFriends().put(userIdB, FriendStatus.PENDING);
        } else {
            LoggedException.throwNew(ExceptionType.INVALID_FRIENDSHIP_ADD, getClass(), List.of(userIdA, userIdB));
        }
    }

    @Override
    public void removeFriend(Integer userIdA, Integer userIdB) {
        Optional<User> userA = Optional.ofNullable(findById(userIdA));
        if (userA.isEmpty() || !userA.get().getFriends().containsKey(userIdB)) {
            LoggedException.throwNew(ExceptionType.INVALID_FRIENDSHIP_REMOVE, getClass(), List.of(userIdA, userIdB));
        }
        userA.get().getFriends().remove(userIdB);
    }
}
