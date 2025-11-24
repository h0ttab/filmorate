package ru.yandex.practicum.filmorate.service;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.model.dto.user.UserUpdateDto;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.util.DtoHelper;
import ru.yandex.practicum.filmorate.util.Validators;

import static ru.yandex.practicum.filmorate.model.FeedEventType.FRIEND;
import static ru.yandex.practicum.filmorate.model.OperationType.ADD;
import static ru.yandex.practicum.filmorate.model.OperationType.REMOVE;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FeedService feedService;
    private final Validators validators;
    private final UserMapper mapper;
    private final DtoHelper dtoHelper;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       FeedService feedService,
                       UserMapper mapper,
                       DtoHelper dtoHelper,
                       Validators validators) {
        this.userStorage = userStorage;
        this.feedService = feedService;
        this.mapper = mapper;
        this.dtoHelper = dtoHelper;
        this.validators = validators;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public Collection<User> getFriends(Integer userId) {
        return userStorage.getFriends(userId);
    }

    public Collection<Feed> getUserFeed(Integer userId) {
        return feedService.findById(userId);
    }

    public Collection<Feed> getAllFeeds() {
        return feedService.findAll();
    }

    public User findById(Integer userId) {
        return userStorage.findById(userId);
    }

    public User create(UserCreateDto userCreateDto) {
        User user = mapper.toEntity(userCreateDto);
        validators.validateLogin(userCreateDto.getLogin(), getClass());
        return userStorage.create(user);
    }

    public User update(UserUpdateDto userUpdateDto) {
        validators.validateUserExists(userUpdateDto.getId(), getClass());
        validators.validateLogin(userUpdateDto.getLogin(), getClass());

        User userUpdate = mapper.toEntity(userUpdateDto);
        User userOriginal = userStorage.findById(userUpdate.getId());

        userUpdate = (User) dtoHelper.transferFields(userOriginal, userUpdate);

        return userStorage.update(userUpdate);
    }

    public void addFriend(Integer userIdA, Integer userIdB) {
        findById(userIdA);
        findById(userIdB);
        validators.validateFriendshipNotExists(userIdA, userIdB, getClass());
        userStorage.addFriend(userIdA, userIdB);
        feedService.save(userIdA, FRIEND, ADD, userIdB);
    }

    public void removeFriend(Integer userIdA, Integer userIdB) {
        findById(userIdA);
        findById(userIdB);
        userStorage.removeFriend(userIdA, userIdB);
        feedService.save(userIdA, FRIEND, REMOVE, userIdB);
    }

    public void delete(Integer userId) {
        userStorage.delete(userId);
    }

    public Set<User> getCommonFriends(Integer userIdA, Integer userIdB) {
        return new HashSet<>(userStorage.getCommonFriends(userIdA, userIdB));
    }
}
