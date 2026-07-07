package com.app.filmorate.service;

import java.util.List;

import com.app.filmorate.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.app.filmorate.model.*;
import com.app.filmorate.storage.feed.FeedDbStorage;
import com.app.filmorate.util.Validators;

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