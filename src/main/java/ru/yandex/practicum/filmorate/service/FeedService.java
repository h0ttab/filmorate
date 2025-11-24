package ru.yandex.practicum.filmorate.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.feed.FeedDbStorage;
import ru.yandex.practicum.filmorate.util.Validators;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedDbStorage feedDbStorage;
    private final Validators validators;

    public List<Feed> findAll() {
        return feedDbStorage.findAll();
    }

    public List<Feed> findById(Integer id) {
        validators.validateUserExists(id, getClass());
        return feedDbStorage.findById(id);
    }

    public void save(Integer userId, FeedEventType feedEventType, OperationType operationType,
                     Integer entityId) {
        this.feedDbStorage.save(userId, feedEventType, operationType, entityId);
    }
}