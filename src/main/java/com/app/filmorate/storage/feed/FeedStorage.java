package com.app.filmorate.storage.feed;

import java.util.List;

import com.app.filmorate.model.*;

public interface FeedStorage {
    List<Feed> findAll();

    List<Feed> findById(Integer id);

    void save(Integer userId, FeedEventType feedEventType, OperationType operationType, Integer entityId);
}
